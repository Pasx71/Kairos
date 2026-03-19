package com.kairos.world;

import com.kairos.KairosPlugin;
import com.kairos.api.DisasterType;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class WorldManager implements Listener {

    private final KairosPlugin plugin;
    private final Map<UUID, WorldProfile> profiles = new HashMap<>();
    private final File worldsFile;
    private FileConfiguration worldsConfig;

    public WorldManager(KairosPlugin plugin) {
        this.plugin = plugin;
        this.worldsFile = new File(plugin.getDataFolder(), "worlds.yml");
        loadWorldsConfig();
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (World world : plugin.getServer().getWorlds()) {
            loadProfile(world);
        }
    }

    private void loadWorldsConfig() {
        if (!worldsFile.exists()) {
            try { worldsFile.createNewFile(); } catch (IOException ignored) {}
        }
        worldsConfig = YamlConfiguration.loadConfiguration(worldsFile);
    }

    public void saveWorldsConfig() {
        try { worldsConfig.save(worldsFile); } catch (IOException e) {
            plugin.getLogger().severe("Failed to save worlds.yml!");
        }
    }

    public WorldProfile getProfile(World world) {
        return profiles.get(world.getUID());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        loadProfile(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        profiles.remove(event.getWorld().getUID());
    }

    private void loadProfile(World world) {
        String path = world.getName() + ".";
        
        boolean naturalSpawning = worldsConfig.getBoolean(path + "natural_spawning", true);
        
        // GAP PATCHED: Safe Difficulty Parsing fallback
        Difficulty difficulty;
        try {
            difficulty = Difficulty.valueOf(worldsConfig.getString(path + "difficulty", "NORMAL").toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            difficulty = Difficulty.NORMAL;
            plugin.getLogger().warning("Invalid difficulty found in worlds.yml for " + world.getName() + ". Defaulting to NORMAL.");
        }
        
        int maxRadius = worldsConfig.getInt(path + "max_radius", 150);
        
        List<String> allowedIds = worldsConfig.getStringList(path + "allowed_disasters");
        HashSet<DisasterType> allowedDisasters = new HashSet<>();
        
        if (allowedIds.isEmpty() && !worldsConfig.contains(path + "allowed_disasters")) {
            // Default setup if world is brand new
            allowedDisasters.add(DisasterType.EXTREME_WINDS);
            allowedDisasters.add(DisasterType.BLIZZARD);
            allowedDisasters.add(DisasterType.SANDSTORM); // Added Sandstorm just in case
        } else {
            for (String id : allowedIds) {
                DisasterType t = DisasterType.forName(id);
                if (t != null) allowedDisasters.add(t);
            }
        }
        
        List<String> whitelisted = worldsConfig.getStringList(path + "whitelisted_players");
        HashSet<UUID> whitelistedPlayers = new HashSet<>();
        for (String uuidStr : whitelisted) {
            try {
                whitelistedPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed UUID strings
            }
        }

        // COMPILE ERROR FIXED: Removed the trailing 'this' parameter
        WorldProfile profile = new WorldProfile(world, naturalSpawning, allowedDisasters, whitelistedPlayers, difficulty, maxRadius);
        profiles.put(world.getUID(), profile);
    }

    // Called by Commands (ToggleCommand, DifficultyCommand, etc.) when a setting changes
    public void saveProfile(WorldProfile profile) {
        String path = profile.getWorld().getName() + ".";
        worldsConfig.set(path + "natural_spawning", profile.isNaturalSpawningAllowed());
        worldsConfig.set(path + "difficulty", profile.getDifficulty().name());
        worldsConfig.set(path + "max_radius", profile.getMaxRadius());
        
        List<String> allowed = profile.getAllowedDisasters().stream().map(DisasterType::getId).collect(Collectors.toList());
        worldsConfig.set(path + "allowed_disasters", allowed);
        
        List<String> whitelisted = profile.getWhitelistedPlayers().stream().map(UUID::toString).collect(Collectors.toList());
        worldsConfig.set(path + "whitelisted_players", whitelisted);
        
        saveWorldsConfig();
    }
}