package nl.ultraspawn.command;

import nl.ultraspawn.UltraSpawnPlugin;
import nl.ultraspawn.config.MessageManager;
import nl.ultraspawn.manager.SpawnManager;
import nl.ultraspawn.manager.TeleportManager;
import nl.ultraspawn.update.UpdateChecker;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class UltraSpawnCommand implements CommandExecutor, TabCompleter {
    private final UltraSpawnPlugin plugin;
    private final MessageManager messages;
    private final SpawnManager spawns;
    private final TeleportManager teleports;
    private final UpdateChecker updates;

    public UltraSpawnCommand(UltraSpawnPlugin plugin, MessageManager messages, SpawnManager spawns,
                             TeleportManager teleports, UpdateChecker updates) {
        this.plugin = plugin;
        this.messages = messages;
        this.spawns = spawns;
        this.teleports = teleports;
        this.updates = updates;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        String sub = args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help" -> help(sender);
            case "reload" -> reload(sender);
            case "version" -> version(sender);
            case "info" -> info(sender);
            default -> messages.send(sender, "general.invalid-subcommand");
        }
        return true;
    }

    private void help(CommandSender sender) {
        sender.sendMessage(messages.get("help.header"));
        sender.sendMessage(messages.get("help.title", Map.of("version", plugin.getDescription().getVersion())));
        sender.sendMessage(messages.get("help.description"));
        sender.sendMessage("");
        if (sender.hasPermission("ultraspawn.spawn")) sender.sendMessage(messages.get("help.spawn"));
        if (sender.hasPermission("ultraspawn.hub")) {
            sender.sendMessage(messages.get("help.hub"));
            sender.sendMessage(messages.get("help.lobby"));
        }
        if (sender.hasPermission("ultraspawn.setspawn")) sender.sendMessage(messages.get("help.setspawn"));
        sender.sendMessage(messages.get("help.help"));
        if (sender.hasPermission("ultraspawn.info")) sender.sendMessage(messages.get("help.info"));
        if (sender.hasPermission("ultraspawn.reload")) sender.sendMessage(messages.get("help.reload"));
        if (sender.hasPermission("ultraspawn.version")) sender.sendMessage(messages.get("help.version"));
        sender.sendMessage(messages.get("help.header"));
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("ultraspawn.reload")) {
            messages.send(sender, "general.no-permission");
            return;
        }
        teleports.cancelAll(false);
        plugin.reloadUltraSpawn();
        messages.send(sender, "general.reloaded");
    }

    private void version(CommandSender sender) {
        if (!sender.hasPermission("ultraspawn.version")) {
            messages.send(sender, "general.no-permission");
            return;
        }
        sender.sendMessage(messages.get("help.header"));
        sender.sendMessage(MessageManager.color("&bUltraSpawn &7v" + plugin.getDescription().getVersion()));
        sender.sendMessage(MessageManager.color("&7Paper: &f" + plugin.getServer().getMinecraftVersion()));
        sender.sendMessage(updates.statusMessage());
        sender.sendMessage(messages.get("help.header"));
    }

    private void info(CommandSender sender) {
        if (!sender.hasPermission("ultraspawn.info")) {
            messages.send(sender, "general.no-permission");
            return;
        }
        sender.sendMessage(messages.get("help.header"));
        sender.sendMessage(MessageManager.color("&bUltraSpawn serverinformatie"));
        sender.sendMessage(MessageManager.color("&7Servernaam: &f" + plugin.getConfig().getString("server.name", "onbekend")));
        sender.sendMessage(MessageManager.color("&7Proxytype: &f" + plugin.getConfig().getString("network.proxy-type", "NONE")));
        sender.sendMessage(MessageManager.color("&7Lobbyserver: &f" + plugin.getConfig().getString("network.lobby-server", "lobby")));
        sender.sendMessage(MessageManager.color("&7Join-teleport: &f" + plugin.getConfig().getBoolean("join.teleport-to-spawn", true)));
        sender.sendMessage(MessageManager.color("&7Teleportvertraging: &f" + plugin.getConfig().getInt("teleport.delay-seconds", 3) + " seconden"));
        Location location = spawns.getSpawn(null).orElse(null);
        if (location == null) {
            sender.sendMessage(MessageManager.color("&7Lokale spawn: &cNiet ingesteld"));
        } else {
            sender.sendMessage(MessageManager.color("&7Lokale spawn: &aIngesteld"));
            sender.sendMessage(MessageManager.color("&7Wereld: &f" + location.getWorld().getName()));
            sender.sendMessage(MessageManager.color(String.format("&7Locatie: &f%.2f, %.2f, %.2f", location.getX(), location.getY(), location.getZ())));
        }
        sender.sendMessage(messages.get("help.header"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) return List.of();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> options = new ArrayList<>();
        options.add("help");
        if (sender.hasPermission("ultraspawn.info")) options.add("info");
        if (sender.hasPermission("ultraspawn.reload")) options.add("reload");
        if (sender.hasPermission("ultraspawn.version")) options.add("version");
        return options.stream().filter(value -> value.startsWith(prefix)).sorted().toList();
    }
}
