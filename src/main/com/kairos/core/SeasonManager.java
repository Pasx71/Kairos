package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.api.DisasterType;
import com.kairos.api.events.SeasonChangeEvent;
import com.kairos.api.seasons.BiomeSeasonEffect;
import com.kairos.api.seasons.Season;
import com.kairos.handlers.protection.ProtectionType;
import com.kairos.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class SeasonManager {

    private final KairosPlugin plugin;
    
    private final Map<Season, Map<Biome, List<BiomeSeasonEffect>>> seasonalBiomeEffects = new HashMap<>();
    private final Map<UUID, SeasonState> activeSeasons = new HashMap<>();
    private final Map<UUID, Long> lastDayChecked = new HashMap<>();

    public SeasonManager(KairosPlugin plugin) {
        this.plugin = plugin;
        for (Season season : Season.values()) {
            seasonalBiomeEffects.put(season, new HashMap<>());
        }
    }

    public Season getCurrentSeason(World world) {
        return getState(world).currentSeason;
    }

    public void checkDayCycle(World world) {
        long currentDay = world.getFullTime() / 24000;
        
        if (!lastDayChecked.containsKey(world.getUID())) {
            lastDayChecked.put(world.getUID(), currentDay);
            return;
        }

        long lastDay = lastDayChecked.get(world.getUID());

        if (currentDay > lastDay) {
            SeasonState state = getState(world);
            state.daysPassed += (int) (currentDay - lastDay);
            
            while (state.daysPassed >= state.targetDuration) {
                state.daysPassed -= state.targetDuration; 
                state.advanceSeason(world);
            }
            
            // DYNAMIC SCHEDULED EVENT TRIGGERS
            FileConfiguration config = plugin.getConfigManager().getConfig("disasters.yml");
            for (DisasterType type : plugin.getDisasterRegistry().getAllRegisteredDisasters()) {
                if (!config.contains(type.getId() + ".scheduled_triggers")) continue;

                List<Map<?, ?>> triggers = config.getMapList(type.getId() + ".scheduled_triggers");
                for (Map<?, ?> trigger : triggers) {
                    String seasonStr = (String) trigger.get("season");
                    int day = trigger.containsKey("day") ? ((Number) trigger.get("day")).intValue() : -1;
                    int level = trigger.containsKey("level") ? ((Number) trigger.get("level")).intValue() : 1;
                    
                    if (state.currentSeason.name().equalsIgnoreCase(seasonStr) && state.daysPassed == day) {
                        for (org.bukkit.entity.Player p : world.getPlayers()) {
                            
                            // INTEGRATION FIX: Ensure the player actually passes the disaster spawn conditions (WorldGuard Claims, Height, Biome)
                            if (plugin.getDisasterRegistry().checkConditions(type, p)) {
                                // Double check the raw Protection Manager so disasters don't spawn in safe zones!
                                if (!plugin.getProtectionManager().isProtected(p.getLocation(), ProtectionType.DISASTER_SPAWN)) {
                                    plugin.getDisasterRegistry().startDisaster(type, level, world, p, true);
                                }
                            }
                        }
                    }
                }
            }
            
            saveState(world, state);
            lastDayChecked.put(world.getUID(), currentDay);
        } else if (currentDay < lastDay) {
            lastDayChecked.put(world.getUID(), currentDay); 
        }
    }

    private SeasonState getState(World world) {
        if (!activeSeasons.containsKey(world.getUID())) {
            loadState(world);
        }
        return activeSeasons.get(world.getUID());
    }

    private void loadState(World world) {
        FileConfiguration data = plugin.getDataFileManager().getConfig();
        String path = "seasons." + world.getUID().toString();
        
        SeasonState state = new SeasonState();
        if (data.contains(path)) {
            state.currentSeason = Season.valueOf(data.getString(path + ".season", "SPRING"));
            state.daysPassed = data.getInt(path + ".days_passed", 0);
            state.targetDuration = data.getInt(path + ".target_duration", generateTargetDuration(state.currentSeason));
        } else {
            long currentDay = world.getFullTime() / 24000;
            state.currentSeason = Season.values()[(int) ((currentDay / 30) % 4)];
            state.daysPassed = (int) (currentDay % 30);
            state.targetDuration = generateTargetDuration(state.currentSeason);
        }
        activeSeasons.put(world.getUID(), state);
    }

    private void saveState(World world, SeasonState state) {
        FileConfiguration data = plugin.getDataFileManager().getConfig();
        String path = "seasons." + world.getUID().toString();
        data.set(path + ".season", state.currentSeason.name());
        data.set(path + ".days_passed", state.daysPassed);
        data.set(path + ".target_duration", state.targetDuration);
        plugin.getDataFileManager().saveConfig();
    }
    
    public void saveAllStates() {
        FileConfiguration data = plugin.getDataFileManager().getConfig();
        for (Map.Entry<UUID, SeasonState> entry : activeSeasons.entrySet()) {
            String path = "seasons." + entry.getKey().toString();
            data.set(path + ".season", entry.getValue().currentSeason.name());
            data.set(path + ".days_passed", entry.getValue().daysPassed);
            data.set(path + ".target_duration", entry.getValue().targetDuration);
        }
        plugin.getDataFileManager().saveConfig();
    }

    public void setSeason(World world, Season season) {
        SeasonState state = getState(world);
        Season oldSeason = state.currentSeason;
        
        // API INTEGRATION
        SeasonChangeEvent event = new SeasonChangeEvent(world, oldSeason, season);
        Bukkit.getPluginManager().callEvent(event);
        
        state.currentSeason = event.getNewSeason(); // Allow other plugins to alter the outcome!
        state.daysPassed = 0;
        state.targetDuration = generateTargetDuration(state.currentSeason);
        saveState(world, state);
        
        MessageUtils.broadcast("messages.seasons.season_forced", "&8[&bKairos&8] &7The season in &e%world% &7was forcefully changed to %season%&7!",
                "%world%", world.getName(), "%season%", state.currentSeason.getDisplayName());
    }

    public int getDaysPassed(World world) { return getState(world).daysPassed; }
    public int getTargetDuration(World world) { return getState(world).targetDuration; }

    private int generateTargetDuration(Season season) {
        Random random = new Random();
        FileConfiguration config = plugin.getConfigManager().getConfig("seasons/" + season.name().toLowerCase() + ".yml");
        
        int baseDays = config.getInt("duration", 30);
        int variance = config.getInt("variance", 5);
        
        int mod = variance > 0 ? random.nextInt(variance * 2 + 1) - variance : 0;
        return Math.max(1, baseDays + mod); 
    }

    public void registerBiomeEffect(BiomeSeasonEffect effect) {
        for (Biome biome : effect.getBiomes()) {
            seasonalBiomeEffects.get(effect.getSeason())
                    .computeIfAbsent(biome, k -> new ArrayList<>())
                    .add(effect);
        }
    }

    public List<BiomeSeasonEffect> getEffects(Season season, Biome biome) {
        return seasonalBiomeEffects.get(season).getOrDefault(biome, new ArrayList<>());
    }

    private class SeasonState {
        Season currentSeason;
        int daysPassed;
        int targetDuration;

        void advanceSeason(World world) {
            int nextIndex = (currentSeason.ordinal() + 1) % 4;
            Season nextSeason = Season.values()[nextIndex];
            
            // API INTEGRATION
            SeasonChangeEvent event = new SeasonChangeEvent(world, currentSeason, nextSeason);
            Bukkit.getPluginManager().callEvent(event);

            currentSeason = event.getNewSeason(); // Allow other plugins to alter the outcome!
            targetDuration = generateTargetDuration(currentSeason);
            
            MessageUtils.broadcast("messages.seasons.season_shifted", "&8[&bKairos&8] &7The season in &e%world% &7has shifted to %season%&7!",
                    "%world%", world.getName(), "%season%", currentSeason.getDisplayName());
        }
    }
}