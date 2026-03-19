package com.kairos.api.seasons;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import java.util.List;

public interface BiomeSeasonEffect {
    
    /** The biomes this effect applies to */
    List<Biome> getBiomes();

    /** The season this effect activates in */
    Season getSeason();

    /** The logic to run on the player (e.g. freezing them, changing weather, spawning particles) */
    void applyEffect(Player player);
}