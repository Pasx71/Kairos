package com.kairos.listeners;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.Season;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class SeasonalWeatherListener implements Listener {

    private final KairosPlugin plugin;

    public SeasonalWeatherListener(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    // priority = LOW allows other plugins (like voting plugins) to override us if they use HIGH
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        Season season = plugin.getSeasonManager().getCurrentSeason(event.getWorld());
        FileConfiguration config = plugin.getConfigManager().getConfig("seasons/" + season.name().toLowerCase() + ".yml");

        if (!config.getBoolean("weather_forcing.enabled", false)) return;

        if (event.toWeatherState()) { 
            // The world is trying to start raining
            double blockRainChance = config.getDouble("weather_forcing.block_rain_chance", 0.0);
            if (Math.random() < blockRainChance) {
                event.setCancelled(true); // Drought mechanic!
            }
        } else {
            // The world is trying to clear the weather
            double blockClearChance = config.getDouble("weather_forcing.block_clear_chance", 0.0);
            if (Math.random() < blockClearChance) {
                event.setCancelled(true); // Endless rain mechanic!
            }
        }
    }
}