package com.kairos.core;

import com.kairos.KairosPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final KairosPlugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> files = new HashMap<>();

    public ConfigManager(KairosPlugin plugin) {
        this.plugin = plugin;
        
        loadConfig("config.yml");
        loadConfig("biomes.yml"); // NEW: Global biome dictionary
        loadConfig("temperature.yml");
        loadConfig("thirst.yml");
        loadConfig("disasters.yml");
        loadConfig("mobs.yml");
        loadConfig("items.yml");    
        loadConfig("messages.yml"); 
        loadConfig("loot.yml");    
        loadConfig("protection.yml");
        loadConfig("gui.yml");
        loadConfig("diseases.yml");

        // NEW: Load Modular Season Files
        loadSeasonConfig("spring.yml");
        loadSeasonConfig("summer.yml");
        loadSeasonConfig("autumn.yml");
        loadSeasonConfig("winter.yml");
    }

    public void loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                plugin.saveResource(fileName, false); 
            } catch (IllegalArgumentException e) {
                try { file.createNewFile(); } catch (IOException ignored) {}
            }
        }
        
        if (configs.containsKey(fileName)) {
            try {
                configs.get(fileName).load(file);
            } catch (Exception e) {
                plugin.getLogger().severe("Could not reload " + fileName + ": " + e.getMessage());
            }
        } else {
            configs.put(fileName, YamlConfiguration.loadConfiguration(file));
        }
        files.put(fileName, file);
    }

    public void loadSeasonConfig(String fileName) {
        File folder = new File(plugin.getDataFolder(), "seasons");
        if (!folder.exists()) folder.mkdirs();
        
        File file = new File(folder, fileName);
        if (!file.exists()) {
            try {
                plugin.saveResource("seasons/" + fileName, false); 
            } catch (IllegalArgumentException e) {
                try { file.createNewFile(); } catch (IOException ignored) {}
            }
        }
        
        String key = "seasons/" + fileName;
        if (configs.containsKey(key)) {
            try {
                configs.get(key).load(file);
            } catch (Exception e) {
                plugin.getLogger().severe("Could not reload " + key + ": " + e.getMessage());
            }
        } else {
            configs.put(key, YamlConfiguration.loadConfiguration(file));
        }
        files.put(key, file);
    }

    public void reloadConfigs() {
        loadConfig("config.yml");
        loadConfig("biomes.yml"); 
        loadConfig("temperature.yml");
        loadConfig("thirst.yml");
        loadConfig("disasters.yml");
        loadConfig("mobs.yml");
        loadConfig("items.yml");    
        loadConfig("messages.yml"); 
        loadConfig("loot.yml");    
        loadConfig("protection.yml");
        loadConfig("gui.yml");
        loadConfig("diseases.yml");

        loadSeasonConfig("spring.yml");
        loadSeasonConfig("summer.yml");
        loadSeasonConfig("autumn.yml");
        loadSeasonConfig("winter.yml");
    }

    public FileConfiguration getConfig(String fileName) {
        return configs.getOrDefault(fileName, plugin.getConfig());
    }

    public void saveConfig(String fileName) {
        if (configs.containsKey(fileName) && files.containsKey(fileName)) {
            try {
                configs.get(fileName).save(files.get(fileName));
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save " + fileName + "!");
            }
        }
    }
}