package nl.ultraspawn.util;

import nl.ultraspawn.UltraSpawnPlugin;
import nl.ultraspawn.config.MessageManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

public final class Effects {
    private Effects() {}

    public static void playSound(UltraSpawnPlugin plugin, Player player, String section) {
        if (!plugin.getConfig().getBoolean("sounds.enabled", true)) return;
        String path = "sounds." + section;
        String name = plugin.getConfig().getString(path + ".sound", "");
        if (name == null || name.isBlank()) return;
        try {
            Sound sound = Sound.valueOf(name.toUpperCase());
            float volume = (float) plugin.getConfig().getDouble(path + ".volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble(path + ".pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Ongeldige sound in " + path + ": " + name);
        }
    }

    public static void particle(UltraSpawnPlugin plugin, Location location, String section) {
        if (!plugin.getConfig().getBoolean("particles.enabled", true)) return;
        String path = "particles." + section;
        String name = plugin.getConfig().getString(path + ".type", "");
        if (name == null || name.isBlank()) return;
        try {
            Particle particle = Particle.valueOf(name.toUpperCase());
            location.getWorld().spawnParticle(
                    particle, location,
                    plugin.getConfig().getInt(path + ".amount", 20),
                    plugin.getConfig().getDouble(path + ".offset-x", 0.4),
                    plugin.getConfig().getDouble(path + ".offset-y", 0.8),
                    plugin.getConfig().getDouble(path + ".offset-z", 0.4),
                    plugin.getConfig().getDouble(path + ".speed", 0.1)
            );
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Ongeldig particle in " + path + ": " + name);
        }
    }

    public static void title(UltraSpawnPlugin plugin, MessageManager messages, Player player,
                             String section, Map<String, ?> replacements) {
        if (!plugin.getConfig().getBoolean("titles.enabled", true)) return;
        String path = "titles." + section;
        String title = replace(plugin.getConfig().getString(path + ".title", ""), replacements);
        String subtitle = replace(plugin.getConfig().getString(path + ".subtitle", ""), replacements);
        player.sendTitle(
                MessageManager.color(title), MessageManager.color(subtitle),
                plugin.getConfig().getInt(path + ".fade-in", 0),
                plugin.getConfig().getInt(path + ".stay", 25),
                plugin.getConfig().getInt(path + ".fade-out", 5)
        );
    }

    private static String replace(String text, Map<String, ?> replacements) {
        String result = text == null ? "" : text;
        for (Map.Entry<String, ?> entry : replacements.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }
}
