package com.kairos.core;

import com.kairos.KairosPlugin;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class BiomeGroupManager {

    private final KairosPlugin plugin;
    private final Map<String, Set<Biome>> biomeGroups = new HashMap<>();

    public BiomeGroupManager(KairosPlugin plugin) {
        this.plugin = plugin;
        loadGroups();
    }

    public void loadGroups() {
        biomeGroups.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("biomes.yml");
        ConfigurationSection section = config.getConfigurationSection("groups");

        if (section == null) return;

        for (String groupName : section.getKeys(false)) {
            Set<Biome> biomes = new HashSet<>();
            
            for (String biomeString : section.getStringList(groupName)) {
                try {
                    biomes.add(Biome.valueOf(biomeString.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[Kairos] Invalid biome '" + biomeString + "' found in biomes.yml under group '" + groupName + "'. Skipping...");
                }
            }
            biomeGroups.put(groupName.toLowerCase(), biomes);
        }
    }

    public boolean isInGroup(Biome biome, String groupName) {
        Set<Biome> group = biomeGroups.get(groupName.toLowerCase());
        return group != null && group.contains(biome);
    }

    public List<Biome> getBiomesInGroup(String groupName) {
        Set<Biome> group = biomeGroups.get(groupName.toLowerCase());
        return group != null ? new ArrayList<>(group) : new ArrayList<>();
    }
}