package nl.ultraspawn;

import nl.ultraspawn.command.SetSpawnCommand;
import nl.ultraspawn.command.SpawnCommand;
import nl.ultraspawn.command.UltraSpawnCommand;
import nl.ultraspawn.config.MessageManager;
import nl.ultraspawn.listener.PlayerListener;
import nl.ultraspawn.manager.SpawnManager;
import nl.ultraspawn.manager.TeleportManager;
import nl.ultraspawn.network.NetworkManager;
import nl.ultraspawn.update.UpdateChecker;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltraSpawnPlugin extends JavaPlugin {
    private MessageManager messages;
    private SpawnManager spawnManager;
    private NetworkManager networkManager;
    private TeleportManager teleportManager;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        messages = new MessageManager(this);
        spawnManager = new SpawnManager(this, messages);
        networkManager = new NetworkManager(this, messages);
        teleportManager = new TeleportManager(this, messages, spawnManager, networkManager);
        updateChecker = new UpdateChecker(this, messages);

        registerCommands();
        getServer().getPluginManager().registerEvents(
                new PlayerListener(this, messages, spawnManager, teleportManager, updateChecker), this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        if (getConfig().getBoolean("update-checker.enabled", true)
                && getConfig().getBoolean("update-checker.check-on-startup", true)) {
            updateChecker.checkAsync();
        }

        getLogger().info("UltraSpawn " + getDescription().getVersion() + " is ingeschakeld.");
    }

    @Override
    public void onDisable() {
        if (teleportManager != null) teleportManager.cancelAll(false);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
    }

    private void registerCommands() {
        SpawnCommand spawnCommand = new SpawnCommand(teleportManager);
        requireCommand("spawn").setExecutor(spawnCommand);
        requireCommand("hub").setExecutor(spawnCommand);

        requireCommand("setspawn").setExecutor(new SetSpawnCommand(messages, spawnManager));

        UltraSpawnCommand admin = new UltraSpawnCommand(this, messages, spawnManager, teleportManager, updateChecker);
        requireCommand("ultraspawn").setExecutor(admin);
        requireCommand("ultraspawn").setTabCompleter(admin);
    }

    private PluginCommand requireCommand(String name) {
        PluginCommand command = getCommand(name);
        if (command == null) throw new IllegalStateException("Command ontbreekt in plugin.yml: " + name);
        return command;
    }

    public void reloadUltraSpawn() {
        reloadConfig();
        messages.reload();
        spawnManager.reload();
        teleportManager.reload();
        updateChecker.reload();
    }

    public MessageManager messages() { return messages; }
    public SpawnManager spawnManager() { return spawnManager; }
    public TeleportManager teleportManager() { return teleportManager; }
    public UpdateChecker updateChecker() { return updateChecker; }
}
