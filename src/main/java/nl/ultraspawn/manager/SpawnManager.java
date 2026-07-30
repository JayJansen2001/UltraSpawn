package nl.ultraspawn.manager;

import nl.ultraspawn.UltraSpawnPlugin;
import nl.ultraspawn.config.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

public final class SpawnManager {
    private final UltraSpawnPlugin plugin;
    private final MessageManager messages;

    public SpawnManager(UltraSpawnPlugin plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void reload() {
        // Locatie wordt rechtstreeks uit de actuele configuratie gelezen.
    }

    public boolean isSet() {
        String world = plugin.getConfig().getString("spawn.world", "");
        return world != null && !world.isBlank();
    }

    public Optional<Location> getSpawn(Player notify) {
        String worldName = plugin.getConfig().getString("spawn.world", "");
        if (worldName == null || worldName.isBlank()) {
            if (notify != null) messages.send(notify, "spawn.not-set");
            return Optional.empty();
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            if (notify != null) messages.send(notify, "spawn.world-not-found", Map.of("world", worldName));
            return Optional.empty();
        }
        return Optional.of(new Location(
                world,
                plugin.getConfig().getDouble("spawn.x"),
                plugin.getConfig().getDouble("spawn.y"),
                plugin.getConfig().getDouble("spawn.z"),
                (float) plugin.getConfig().getDouble("spawn.yaw"),
                (float) plugin.getConfig().getDouble("spawn.pitch")
        ));
    }

    public void setSpawn(Location location) {
        plugin.getConfig().set("spawn.world", location.getWorld().getName());
        plugin.getConfig().set("spawn.x", location.getX());
        plugin.getConfig().set("spawn.y", location.getY());
        plugin.getConfig().set("spawn.z", location.getZ());
        plugin.getConfig().set("spawn.yaw", location.getYaw());
        plugin.getConfig().set("spawn.pitch", location.getPitch());
        plugin.saveConfig();
    }
}
