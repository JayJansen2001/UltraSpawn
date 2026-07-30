package nl.ultraspawn.network;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import nl.ultraspawn.UltraSpawnPlugin;
import nl.ultraspawn.config.MessageManager;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

public final class NetworkManager {
    private final UltraSpawnPlugin plugin;
    private final MessageManager messages;

    public NetworkManager(UltraSpawnPlugin plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public boolean isEnabled() {
        String type = plugin.getConfig().getString("network.proxy-type", "NONE");
        return plugin.getConfig().getBoolean("network.enabled", true)
                && type != null && !type.equalsIgnoreCase("NONE");
    }

    public String lobbyServer() {
        return plugin.getConfig().getString("network.lobby-server", "lobby");
    }

    public boolean isCurrentServerLobby() {
        String current = plugin.getConfig().getString("server.name", "");
        return current != null && current.equalsIgnoreCase(lobbyServer());
    }

    public boolean connectToLobby(Player player) {
        if (!isEnabled()) {
            messages.send(player, "network.disabled");
            return false;
        }
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(lobbyServer());
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            messages.send(player, "network.connecting", Map.of("server", lobbyServer()));
            return true;
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("Kon speler niet naar lobby sturen: " + ex.getMessage());
            messages.send(player, "network.failed");
            return false;
        }
    }

    public String proxyType() {
        return plugin.getConfig().getString("network.proxy-type", "NONE").toUpperCase(Locale.ROOT);
    }
}
