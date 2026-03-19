package com.kairos.world;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;
import java.util.Random;

public enum Difficulty {
    EASY, NORMAL, HARD, EXTREME, CUSTOM;

    private int offset;
    private int timerBase;
    private int[] levelWeights;

    public static void loadConfig(FileConfiguration config) {
        for (Difficulty diff : values()) {
            String path = "difficulties." + diff.name().toLowerCase();
            
            // Fallbacks in case config is missing
            diff.timerBase = config.getInt(path + ".timer_base", 3600);
            diff.offset = config.getInt(path + ".offset", 900);
            
            List<Integer> weights = config.getIntegerList(path + ".level_weights");
            if (weights != null && weights.size() == 6) {
                diff.levelWeights = weights.stream().mapToInt(i -> i).toArray();
            } else {
                diff.levelWeights = new int[]{20, 20, 20, 20, 10, 10}; // Fallback
            }
        }
    }

    public int getOffset() { return offset; }
    public int getTimerBase() { return timerBase; }

    public int simulateLevel(Random random) {
        int totalWeight = 0;
        for (int weight : levelWeights) totalWeight += weight;

        int randomValue = random.nextInt(totalWeight) + 1;
        int currentWeight = 0;

        for (int i = 0; i < levelWeights.length; i++) {
            currentWeight += levelWeights[i];
            if (randomValue <= currentWeight) {
                return i + 1; 
            }
        }
        return 1; 
    }
}