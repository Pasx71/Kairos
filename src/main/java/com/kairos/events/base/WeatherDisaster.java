package com.kairos.events.base;

import com.kairos.api.DisasterType;
import org.bukkit.World;
import java.util.ArrayDeque;
import java.util.Queue;

public abstract class WeatherDisaster extends ActiveDisaster {

    protected int delay, time;
    protected double volume;
    protected boolean regionWeather;

    public static Queue<World> currentWorlds = new ArrayDeque<>();

    public WeatherDisaster(DisasterType type, int level, World world) {
        super(type, level, world);
        
        // GAP PATCHED: Removes the hardcoded EXTREME_WINDS string for OCP compliance
        if (this.level > type.getMaxLevel()) {
            this.level = type.getMaxLevel();
        }
    }

    public void updateWeatherSettings() {
        if (worldProfile != null) {
            regionWeather = (boolean) worldProfile.getSettings().getOrDefault("ignore_weather_effects_in_regions", false);
        }
    }
    
    public int getTime() { return time; }
    public void setTime(int time) { this.time = time; }
    
    public int getDelay() { return delay; }
    public void setDelay(int delay) { this.delay = delay; }
    
    public boolean isRegionWeatherEffects() { return regionWeather; }
    public void setRegionWeatherEffects(boolean value) { this.regionWeather = value; }
    
    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }
}