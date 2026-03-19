package com.kairos.core;

import com.kairos.api.DisasterType;
import com.kairos.api.conditions.SpawnCondition;
import com.kairos.api.seasons.Season;
import com.kairos.events.base.ActiveDisaster;
import com.kairos.KairosPlugin;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class DisasterRegistry {

    @FunctionalInterface
    public interface DisasterSpawner {
        ActiveDisaster create(int level, World world);
    }

    private final Map<DisasterType, DisasterSpawner> spawners = new HashMap<>();
    private final Map<DisasterType, List<SpawnCondition>> conditions = new HashMap<>();
    private final FileConfiguration config;

    public DisasterRegistry(FileConfiguration config) {
        this.config = config;
    }

    public void registerDisaster(DisasterType type, DisasterSpawner spawner) {
        int delay = config.getInt(type.getId() + ".delay_ticks", 20);
        double chance = config.getDouble(type.getId() + ".chance", type.getChance());
        int minHeight = config.getInt(type.getId() + ".min_height", 0);
        int maxLevel = config.getInt(type.getId() + ".max_level", type.getMaxLevel());
        type.setConfigValues(delay, chance, minHeight, maxLevel);

        spawners.put(type, spawner);
        conditions.put(type, new ArrayList<>());
        
        // 1. Global Toggle Check
        addCondition(type, player -> config.getBoolean(type.getId() + ".enabled", true));
        
        // 2. Base Height Condition
        addCondition(type, player -> player.getLocation().getBlockY() >= type.getMinHeight());
        
        // 3. NEW: Dynamic Biome Conditions!
        addCondition(type, player -> {
            String currentBiome = player.getLocation().getBlock().getBiome().name();
            List<String> allowed = config.getStringList(type.getId() + ".allowed_biomes");
            List<String> blacklisted = config.getStringList(type.getId() + ".blacklisted_biomes");
            
            if (!allowed.isEmpty() && !allowed.contains(currentBiome)) return false;
            if (blacklisted.contains(currentBiome)) return false;
            
            return true;
        });
    }

    public void addCondition(DisasterType type, SpawnCondition condition) {
        conditions.computeIfAbsent(type, k -> new ArrayList<>()).add(condition);
    }

    public void reloadSettings() {
        for (DisasterType type : spawners.keySet()) {
            int delay = config.getInt(type.getId() + ".delay_ticks", type.getDelayTicks());
            double chance = config.getDouble(type.getId() + ".chance", type.getChance());
            int minHeight = config.getInt(type.getId() + ".min_height", type.getMinHeight());
            int maxLevel = config.getInt(type.getId() + ".max_level", type.getMaxLevel());
            type.setConfigValues(delay, chance, minHeight, maxLevel);
        }
    }

    public void linkDisasterToSeasons(DisasterType type, Season... seasons) {
        Set<Season> allowedSeasons = EnumSet.copyOf(Arrays.asList(seasons));
        
        addCondition(type, player -> {
            KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
            Season currentSeason = plugin.getSeasonManager().getCurrentSeason(player.getWorld());
            return allowedSeasons.contains(currentSeason);
        });
    }

    public boolean checkConditions(DisasterType type, Player player) {
        List<SpawnCondition> rules = conditions.get(type);
        if (rules == null) return false;

        for (SpawnCondition rule : rules) {
            if (!rule.test(player)) return false;
        }
        return true;
    }

    public void startDisaster(DisasterType type, int level, World world, Player target, boolean broadcast) {
        DisasterSpawner spawner = spawners.get(type);
        if (spawner != null) {
            ActiveDisaster activeDisaster = spawner.create(level, world);
            activeDisaster.start(world, target, broadcast); 
        }
    }

    public Iterable<DisasterType> getAllRegisteredDisasters() {
        return spawners.keySet();
    }
}