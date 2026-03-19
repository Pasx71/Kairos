package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.BiomeSeasonEffect;
import com.kairos.api.seasons.Season;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.List;

public class SeasonTask implements Runnable {

    private final KairosPlugin plugin;

    public SeasonTask(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            
            // NEW: Advance the day cycle & change season if necessary!
            plugin.getSeasonManager().checkDayCycle(world);

            Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);

            for (Player player : world.getPlayers()) {
                Biome playerBiome = player.getLocation().getBlock().getBiome();
                List<BiomeSeasonEffect> effects = plugin.getSeasonManager().getEffects(currentSeason, playerBiome);

                for (BiomeSeasonEffect effect : effects) {
                    effect.applyEffect(player);
                }
            }
        }
    }
}