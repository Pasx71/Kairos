package com.kairos.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

// GAP PATCHED: Clean utility for managing custom yml files (like votes.yml/data.yml)
public class DataFileManager {
    
    private final File file;
    private FileConfiguration config;
    private final Plugin plugin;

    public DataFileManager(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName);
        loadConfig();
    }

    public void loadConfig() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try { config.save(file); } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + file.getName() + "!");
        }
    }
}