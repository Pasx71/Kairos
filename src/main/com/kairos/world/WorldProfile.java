package com.kairos.world;

import com.kairos.api.DisasterType;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Merged the features of 'WorldObject' into 'WorldProfile' for centralization
public class WorldProfile {
    
    private final World world;
    private boolean naturalSpawningAllowed;
    private final Set<DisasterType> allowedDisasters;
    private final Set<UUID> whitelistedPlayers;
    private Difficulty difficulty;
    private final int maxRadius;
    
    private final Map<String, Object> settings = new HashMap<>();
    private final Set<EntityType> blacklistedEntities = new HashSet<>();

    public WorldProfile(World world, boolean naturalSpawningAllowed, Set<DisasterType> allowedDisasters, 
                        Set<UUID> whitelistedPlayers, Difficulty difficulty, int maxRadius) {
        this.world = world;
        this.naturalSpawningAllowed = naturalSpawningAllowed;
        this.allowedDisasters = allowedDisasters;
        this.whitelistedPlayers = whitelistedPlayers;
        this.difficulty = difficulty;
        this.maxRadius = maxRadius;
        
        // Defaults (You can load these via your worlds.yml later)
        settings.put("event_broadcast", true);
        settings.put("ignore_weather_effects_in_regions", false);
    }

    public World getWorld() { return world; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getMaxRadius() { return maxRadius; }
    
    public Map<String, Object> getSettings() { return settings; }
    public Set<EntityType> getBlacklistedEntities() { return blacklistedEntities; }

    public boolean canSpawn(DisasterType disaster) {
        return naturalSpawningAllowed && allowedDisasters.contains(disaster);
    }

    public boolean isPlayerWhitelisted(UUID uuid) {
        return whitelistedPlayers.contains(uuid);
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setNaturalSpawningAllowed(boolean allowed) {
        this.naturalSpawningAllowed = allowed;
    }

    public boolean isNaturalSpawningAllowed() {
        return naturalSpawningAllowed;
    }

    public void addAllowedDisaster(DisasterType type) {
        this.allowedDisasters.add(type);
    }

    public void removeAllowedDisaster(DisasterType type) {
        this.allowedDisasters.remove(type);
    }

    public void addWhitelistedPlayer(UUID uuid) {
        this.whitelistedPlayers.add(uuid);
    }

    public void removeWhitelistedPlayer(UUID uuid) {
        this.whitelistedPlayers.remove(uuid);
    }
    
    public Set<DisasterType> getAllowedDisasters() {
        return allowedDisasters;
    }
    
    public Set<UUID> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }
}