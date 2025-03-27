package ir.alireza009.koyaGPS;

import ir.alireza009.koyaGPS.command.GPSCommand;
import ir.alireza009.koyaGPS.command.TabComplete;
import ir.alireza009.koyaGPS.listener.PlayerInteractListener;
import ir.alireza009.koyaGPS.listener.PlayerJoinListener;
import ir.alireza009.koyaGPS.listener.PlayerMoveListener;
import ir.alireza009.koyaGPS.listener.PlayerQuitListener;
import ir.alireza009.koyaGPS.storage.ConfigFiles;
import ir.alireza009.koyaGPS.task.PlayersTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Callable;

public final class KoyaGPS extends JavaPlugin {
    private static Economy economy = null;
    public static Economy getEconomy() {
        return economy;
    }

    private static KoyaGPS plugin;
    public static KoyaGPS getInstance() { return plugin; }

    private static ConfigFiles locationFileManager;
    public static ConfigFiles getLocationFileManager() { return locationFileManager; }

    private static ConfigFiles playersFileManager;
    public static ConfigFiles getPlayersFileManager() { return playersFileManager; }

    private static ConfigFiles langFileManager;
    public static ConfigFiles getLangFileManager() { return langFileManager; }

    @Override
    public void onEnable() {
        plugin = this;
        // Plugin startup logic
        plugin.saveDefaultConfig();
        new PlayersTask(); // Add this in your onEnable() method
        registerCommands();
        registerListeners();
        registerFileManagers();

        if (KoyaGPS.getInstance().getConfig().getBoolean("FastTravel.Vault")) {
            String[] dependencies = {"Vault"};

            for (String dependency : dependencies) {
                if (!isPluginInstalled(dependency)) {
                    getLogger().severe("Dependency missing: " + dependency);
                    getLogger().severe("Disabling plugin...");
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                } else {
                    //getLogger().severe("Dependency connected: " + dependency);
                }
            }
            setupEconomy();
        }

        int pluginId = 24717;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new Metrics.SimplePie("CurveModel", () -> getConfig().get("CurveModel", true).toString()));
        metrics.addCustomChart(new Metrics.SimplePie("Bossbar", () -> getConfig().get("Bossbar", true).toString()));
        metrics.addCustomChart(new Metrics.SimplePie("Actionbar", () -> getConfig().get("Actionbar", true).toString()));
        metrics.addCustomChart(new Metrics.SimplePie("ArrowGuide", () -> getConfig().get("ArrowGuide", true).toString()));
        metrics.addCustomChart(new Metrics.SimplePie("ArrowVisibleToOtherPlayers", () -> getConfig().get("ArrowVisibleToOtherPlayers", true).toString()));
        metrics.addCustomChart(new Metrics.SimplePie("ServerName", () -> getConfig().get("ServerName", "").toString()));
    }

    @Override
    public void onDisable() {
        PlayersTask.stopPlayersGPS();
        plugin = null;
        // Plugin shutdown logic
    }

    public void registerCommands() {
        getCommand("gps").setExecutor(new GPSCommand());
        getCommand("gps").setTabCompleter(new TabComplete());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
    }

    private void registerFileManagers() {
        locationFileManager = new ConfigFiles(getDataFolder(), "locations.yml");
        locationFileManager.saveDefaultConfig();

        playersFileManager = new ConfigFiles(getDataFolder(), "players.yml");
        playersFileManager.saveDefaultConfig();

        langFileManager = new ConfigFiles(getDataFolder(), "lang.yml");
        langFileManager.saveDefaultConfig();
    }

    private boolean isPluginInstalled(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    private void setupEconomy() {
        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
    }
}
