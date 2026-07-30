package nl.ultraspawn.listener;

import nl.ultraspawn.UltraSpawnPlugin;
import nl.ultraspawn.config.MessageManager;
import nl.ultraspawn.manager.SpawnManager;
import nl.ultraspawn.manager.TeleportManager;
import nl.ultraspawn.update.UpdateChecker;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {
    private final UltraSpawnPlugin plugin;
    private final MessageManager messages;
    private final SpawnManager spawns;
    private final TeleportManager teleports;
    private final UpdateChecker updates;

    public PlayerListener(UltraSpawnPlugin plugin, MessageManager messages, SpawnManager spawns,
                          TeleportManager teleports, UpdateChecker updates) {
        this.plugin = plugin;
        this.messages = messages;
        this.spawns = spawns;
        this.teleports = teleports;
        this.updates = updates;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean firstOnly = plugin.getConfig().getBoolean("join.only-first-join", false);
        if (plugin.getConfig().getBoolean("join.teleport-to-spawn", true)
                && (!firstOnly || !player.hasPlayedBefore())) {
            long delay = Math.max(0L, plugin.getConfig().getLong("join.delay-ticks", 5L));
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                Location spawn = spawns.getSpawn(null).orElse(null);
                if (spawn != null) player.teleport(spawn);
            }, delay);
        }

        if (plugin.getConfig().getBoolean("update-checker.notify-operators-on-join", true)
                && player.hasPermission(plugin.getConfig().getString(
                        "update-checker.notify-permission", "ultraspawn.update"))) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> updates.notifyPlayer(player), 40L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        teleports.cancelForMove(event.getPlayer(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) teleports.cancelForDamage(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        teleports.cancel(event.getPlayer(), null, false);
    }
}
