package nl.ultraspawn.manager;

import nl.ultraspawn.UltraSpawnPlugin;
import nl.ultraspawn.config.MessageManager;
import nl.ultraspawn.network.NetworkManager;
import nl.ultraspawn.util.Effects;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TeleportManager {
    public enum Destination { LOCAL_SPAWN, LOBBY }

    private final UltraSpawnPlugin plugin;
    private final MessageManager messages;
    private final SpawnManager spawnManager;
    private final NetworkManager networkManager;
    private final Map<UUID, PendingTeleport> pending = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public TeleportManager(UltraSpawnPlugin plugin, MessageManager messages,
                           SpawnManager spawnManager, NetworkManager networkManager) {
        this.plugin = plugin;
        this.messages = messages;
        this.spawnManager = spawnManager;
        this.networkManager = networkManager;
    }

    public void reload() {
        cancelAll(false);
    }

    public void request(Player player, Destination destination) {
        if (pending.containsKey(player.getUniqueId())) {
            messages.send(player, "teleport.already-teleporting");
            return;
        }
        if (!player.hasPermission("ultraspawn.bypass.cooldown")) {
            long remaining = remainingCooldown(player);
            if (remaining > 0) {
                messages.send(player, "teleport.cooldown", Map.of("seconds", remaining));
                return;
            }
        }

        if (destination == Destination.LOCAL_SPAWN && spawnManager.getSpawn(player).isEmpty()) return;
        if (destination == Destination.LOBBY && networkManager.isCurrentServerLobby()
                && spawnManager.getSpawn(player).isEmpty()) return;

        int delay = player.hasPermission("ultraspawn.bypass.delay")
                ? 0 : Math.max(0, plugin.getConfig().getInt("teleport.delay-seconds", 3));
        if (delay == 0) {
            complete(player, destination);
            return;
        }

        PendingTeleport state = new PendingTeleport(player.getLocation().clone(), destination, delay);
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> tick(player), 0L, 20L);
        state.task = task;
        pending.put(player.getUniqueId(), state);
    }

    private void tick(Player player) {
        PendingTeleport state = pending.get(player.getUniqueId());
        if (state == null) return;
        if (!player.isOnline()) {
            cancel(player, null, false);
            return;
        }
        if (state.secondsLeft <= 0) {
            pending.remove(player.getUniqueId());
            state.task.cancel();
            complete(player, state.destination);
            return;
        }

        messages.send(player, "teleport.countdown", Map.of("seconds", state.secondsLeft));
        Effects.playSound(plugin, player, "countdown");
        Effects.title(plugin, messages, player, "countdown", Map.of("seconds", state.secondsLeft));
        state.secondsLeft--;
    }

    private void complete(Player player, Destination destination) {
        boolean success;
        if (destination == Destination.LOBBY && !networkManager.isCurrentServerLobby()) {
            success = networkManager.connectToLobby(player);
        } else {
            Location spawn = spawnManager.getSpawn(player).orElse(null);
            if (spawn == null) return;
            Effects.particle(plugin, player.getLocation(), "before");
            success = player.teleport(spawn);
            if (success) {
                Effects.particle(plugin, spawn, "after");
                Effects.playSound(plugin, player, "success");
                Effects.title(plugin, messages, player, "success", Map.of());
                messages.send(player, "teleport.success");
            }
        }
        if (success) startCooldown(player);
    }

    public void cancelForMove(Player player, Location to) {
        if (!plugin.getConfig().getBoolean("teleport.cancel-on-move", true)) return;
        PendingTeleport state = pending.get(player.getUniqueId());
        if (state == null || to == null) return;
        double tolerance = Math.max(0.0, plugin.getConfig().getDouble("teleport.movement-tolerance", 0.05));
        Location from = state.start;
        if (from.getWorld() != to.getWorld()
                || Math.abs(from.getX() - to.getX()) > tolerance
                || Math.abs(from.getY() - to.getY()) > tolerance
                || Math.abs(from.getZ() - to.getZ()) > tolerance) {
            cancel(player, "teleport.cancelled-move", true);
        }
    }

    public void cancelForDamage(Player player) {
        if (plugin.getConfig().getBoolean("teleport.cancel-on-damage", true)) {
            cancel(player, "teleport.cancelled-damage", true);
        }
    }

    public void cancel(Player player, String messagePath, boolean effects) {
        PendingTeleport state = pending.remove(player.getUniqueId());
        if (state == null) return;
        if (state.task != null) state.task.cancel();
        if (messagePath != null) messages.send(player, messagePath);
        if (effects) {
            Effects.playSound(plugin, player, "cancelled");
            Effects.title(plugin, messages, player, "cancelled", Map.of());
        }
    }

    public void cancelAll(boolean effects) {
        for (UUID uuid : pending.keySet().toArray(UUID[]::new)) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) cancel(player, null, effects);
        }
        pending.clear();
    }

    private void startCooldown(Player player) {
        int seconds = Math.max(0, plugin.getConfig().getInt("teleport.cooldown-seconds", 5));
        if (seconds > 0) cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
    }

    private long remainingCooldown(Player player) {
        long until = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long remainingMs = until - System.currentTimeMillis();
        if (remainingMs <= 0) {
            cooldowns.remove(player.getUniqueId());
            return 0;
        }
        return (remainingMs + 999L) / 1000L;
    }

    private static final class PendingTeleport {
        private final Location start;
        private final Destination destination;
        private int secondsLeft;
        private BukkitTask task;

        private PendingTeleport(Location start, Destination destination, int secondsLeft) {
            this.start = start;
            this.destination = destination;
            this.secondsLeft = secondsLeft;
        }
    }
}
