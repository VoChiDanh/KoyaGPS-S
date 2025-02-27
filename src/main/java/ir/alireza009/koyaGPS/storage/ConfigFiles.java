package ir.alireza009.koyaGPS.storage;

import ir.alireza009.koyaGPS.KoyaGPS;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class ConfigFiles {
    private final File folder;
    private final String fileName;
    private File configFile = null;
    private FileConfiguration config = null;

    public ConfigFiles(File folder, String fileName) {
        this.folder = folder;
        this.fileName = fileName;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.configFile == null) this.configFile = new File(folder, fileName);
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        InputStream defaultStream = KoyaGPS.getInstance().getResource(fileName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.config.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (this.config == null) reloadConfig();
        return this.config;
    }

    public void save() {
        if (this.config == null || this.configFile == null) return;
        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            KoyaGPS.getInstance().getLogger().log(Level.SEVERE, "Couldn't save config to " + this.configFile, e);
        }
    }

    public void saveDefaultConfig() {
        if (this.configFile == null) this.configFile = new File(folder, fileName);
        if (!(this.configFile.exists())) {
            try {
                KoyaGPS.getInstance().saveResource(fileName, false);
            } catch (IllegalArgumentException e) {
                try {
                    this.configFile.createNewFile();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}
