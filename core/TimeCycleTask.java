package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.Season;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimeCycleTask implements Runnable {

    private final KairosPlugin plugin;
    private final Map<UUID, Double> timeAccumulator = new HashMap<>();

    public TimeCycleTask(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            
            // Lock vanilla daylight cycle if not already locked
            Boolean rule = world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE);
            if (rule != null && rule) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            }

            Season season = plugin.getSeasonManager().getCurrentSeason(world);
            FileConfiguration config = plugin.getConfigManager().getConfig("seasons/" + season.name().toLowerCase() + ".yml");
            
            long currentTime = world.getTime();
            boolean isDay = currentTime < 12000; // 0 to 12000 is daytime
            
            double multiplier = isDay ? 
                    config.getDouble("time.day_speed_multiplier", 1.0) : 
                    config.getDouble("time.night_speed_multiplier", 1.0);

            double accumulated = timeAccumulator.getOrDefault(world.getUID(), 0.0) + multiplier;
            
            if (accumulated >= 1.0) {
                long ticksToAdd = (long) accumulated;
                world.setFullTime(world.getFullTime() + ticksToAdd);
                accumulated -= ticksToAdd;
            }
            
            timeAccumulator.put(world.getUID(), accumulated);
        }
    }
}