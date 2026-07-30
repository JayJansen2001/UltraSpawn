package nl.ultraspawn.command;

import nl.ultraspawn.manager.TeleportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SpawnCommand implements CommandExecutor {
    private final TeleportManager teleports;

    public SpawnCommand(TeleportManager teleports) {
        this.teleports = teleports;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dit commando kan alleen door spelers worden gebruikt.");
            return true;
        }
        TeleportManager.Destination destination = command.getName().equalsIgnoreCase("spawn")
                ? TeleportManager.Destination.LOCAL_SPAWN
                : TeleportManager.Destination.LOBBY;
        teleports.request(player, destination);
        return true;
    }
}
