package com.kairos.seasons.effects;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.BiomeSeasonEffect;
import com.kairos.api.seasons.Season;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class WinterTaigaEffect implements BiomeSeasonEffect {

    @Override
    public List<Biome> getBiomes() {
        return JavaPlugin.getPlugin(KairosPlugin.class).getBiomeGroupManager().getBiomesInGroup("cold");
    }

    @Override
    public Season getSeason() { return Season.WINTER; }

    @Override
    public void applyEffect(Player player) {
        if (player.getLocation().getBlock().getLightFromBlocks() < 8) {
            player.setFreezeTicks(player.getFreezeTicks() + 40); 
        } else {
            player.setFreezeTicks(Math.max(0, player.getFreezeTicks() - 20));
        }
    }
}