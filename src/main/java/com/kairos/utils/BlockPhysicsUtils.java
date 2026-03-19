package com.kairos.utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class BlockPhysicsUtils {

    private static final Map<Material, Double> materialStrength = new EnumMap<>(Material.class);
    private static final Random random = new Random();

    public static void loadStrengths(File configFile) {
        materialStrength.clear();
        if (!configFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        for (String key : config.getKeys(false)) {
            try {
                Material mat = Material.valueOf(key.toUpperCase());
                materialStrength.put(mat, config.getDouble(key));
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid materials from older configs
            }
        }
    }

    /** Simulates wind/earthquake damage. Returns true if the block survives. */
    public static boolean survivesImpact(Material material) {
        double strength = materialStrength.getOrDefault(material, 0.1); // Default fragile
        if (strength >= 1.0) return true; // Immune block (like Obsidian)
        
        return random.nextDouble() < strength;
    }

    public static boolean isImmune(Material material) {
        return materialStrength.getOrDefault(material, 0.0) >= 1.0;
    }
}