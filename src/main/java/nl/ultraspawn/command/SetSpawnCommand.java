package nl.ultraspawn.command;

import nl.ultraspawn.config.MessageManager;
import nl.ultraspawn.manager.SpawnManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class SetSpawnCommand implements CommandExecutor {
    private final MessageManager messages;
    private final SpawnManager spawns;

    public SetSpawnCommand(MessageManager messages, SpawnManager spawns) {
        this.messages = messages;
        this.spawns = spawns;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "general.players-only");
            return true;
        }
        Location location = player.getLocation();
        spawns.setSpawn(location);
        messages.send(player, "spawn.set", Map.of(
                "world", location.getWorld().getName(),
                "x", String.format("%.2f", location.getX()),
                "y", String.format("%.2f", location.getY()),
                "z", String.format("%.2f", location.getZ())
        ));
        return true;
    }
}
