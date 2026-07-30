package nl.ultraspawn.config;

import nl.ultraspawn.UltraSpawnPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public final class MessageManager {
    private final UltraSpawnPlugin plugin;
    private FileConfiguration config;

    public MessageManager(UltraSpawnPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        config = YamlConfiguration.loadConfiguration(file);
    }

    public String raw(String path) {
        return config.getString(path, "&cOntbrekend bericht: " + path);
    }

    public String get(String path) {
        return color(raw(path));
    }

    public String get(String path, Map<String, ?> replacements) {
        String text = raw(path);
        for (Map.Entry<String, ?> entry : replacements.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return color(text);
    }

    public String prefixed(String path) {
        return get("prefix") + get(path);
    }

    public String prefixed(String path, Map<String, ?> replacements) {
        return get("prefix") + get(path, replacements);
    }

    public void send(CommandSender sender, String path) {
        sender.sendMessage(prefixed(path));
    }

    public void send(CommandSender sender, String path, Map<String, ?> replacements) {
        sender.sendMessage(prefixed(path, replacements));
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
