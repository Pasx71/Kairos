package com.kairos.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DisasterType {
    
    private static final Map<String, DisasterType> REGISTRY = new HashMap<>();

    // Base Disasters (Chance = 1.0)
    public static final DisasterType EXTREME_WINDS = register("extreme_winds", "Extreme Winds", 6, 1.0);
    public static final DisasterType BLIZZARD = register("blizzard", "Blizzard", 5, 1.0);
    public static final DisasterType SANDSTORM = register("sandstorm", "Sandstorm", 5, 1.0);
    public static final DisasterType EARTHQUAKE = register("earthquake", "Earthquake", 6, 1.0);
    public static final DisasterType CAVE_IN = register("cave_in", "Cave In", 6, 1.0);
    
    // NEW ONES PORTED OVER!
    public static final DisasterType TORNADO = register("tornado", "Tornado", 6, 1.0);
    public static final DisasterType HURRICANE = register("hurricane", "Hurricane", 6, 1.0);

    // Purges & Invasions (Custom Base Weights)
    public static final DisasterType INFESTATION = register("infestation", "Infestation", 5, 0.8);
    public static final DisasterType NETHER_PURGE = register("nether_purge", "Nether Purge", 5, 0.6);
    public static final DisasterType BANDIT_RAID = register("bandit_raid", "Bandit Raid", 5, 0.3);
    public static final DisasterType DEATH_PARADE = register("death_parade", "Death Parade", 5, 0.0);
    
    private final String id;
    private final String displayName;

    private int delayTicks = 20;
    private double chance = 1.0;
    private int minHeight = 0;
    private int maxLevel;

    private DisasterType(String id, String displayName, int maxLevel) {
        this.id = id;
        this.displayName = displayName;
        this.maxLevel = maxLevel;
    }

    public static DisasterType register(String id, String displayName, int defaultMaxLevel, double defaultChance) {
        DisasterType type = new DisasterType(id.toLowerCase(), displayName, defaultMaxLevel);
        type.chance = defaultChance;
        REGISTRY.put(type.getId(), type);
        return type;
    }

    public static DisasterType forName(String id) {
        if (id == null) return null;
        return REGISTRY.get(id.toLowerCase());
    }

    public static Collection<DisasterType> values() {
        return REGISTRY.values();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getDelayTicks() { return delayTicks; }
    public double getChance() { return chance; }
    public int getMinHeight() { return minHeight; }
    public int getMaxLevel() { return maxLevel; }

    public void setConfigValues(int delayTicks, double chance, int minHeight, int maxLevel) {
        this.delayTicks = delayTicks;
        this.chance = chance;
        this.minHeight = minHeight;
        this.maxLevel = maxLevel;
    }
}
