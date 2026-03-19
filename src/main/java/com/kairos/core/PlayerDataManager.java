package com.kairos.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    
    private final File file;
    private final FileConfiguration data;
    private final Map<UUID, Integer> activeTimers = new HashMap<>();

    public PlayerDataManager(Plugin plugin) {
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public int getTimer(UUID uuid, int defaultTimer) {
        // Return memory cache if available, otherwise check file, otherwise use default
        return activeTimers.getOrDefault(uuid, data.getInt(uuid.toString() + ".timer", defaultTimer));
    }

    public void setTimer(UUID uuid, int ticks) {
        activeTimers.put(uuid, ticks); // Update memory cache (fast, runs every tick)
    }

    public void savePlayer(UUID uuid) {
        if (activeTimers.containsKey(uuid)) {
            data.set(uuid.toString() + ".timer", activeTimers.get(uuid));
            activeTimers.remove(uuid); // Clear from memory to prevent leaks
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, Integer> entry : activeTimers.entrySet()) {
            data.set(entry.getKey().toString() + ".timer", entry.getValue());
        }
        try { data.save(file); } catch (IOException ignored) {}
    }
}