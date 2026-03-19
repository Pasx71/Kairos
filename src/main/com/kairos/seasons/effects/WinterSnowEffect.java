package com.kairos.seasons.effects;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.BiomeSeasonEffect;
import com.kairos.api.seasons.Season;
import com.kairos.handlers.protection.ProtectionType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WinterSnowEffect implements BiomeSeasonEffect {

    private final Random random = new Random();

    @Override
    public List<Biome> getBiomes() {
        KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
        List<Biome> biomes = new ArrayList<>(Arrays.asList(Biome.values()));
        
        biomes.removeAll(plugin.getBiomeGroupManager().getBiomesInGroup("hot"));
        biomes.removeAll(plugin.getBiomeGroupManager().getBiomesInGroup("magical"));
        
        return biomes;
    }

    @Override
    public Season getSeason() { return Season.WINTER; }

    @Override
    public void applyEffect(Player player) {
        if (player.getWorld().hasStorm()) {
            player.getWorld().spawnParticle(
                    Particle.SNOWFLAKE,
                    player.getLocation().add(0, 6, 0), 
                    60, 15, 6, 15, 0.02
            );

            if (random.nextDouble() < 0.10) {
                int xOffset = random.nextInt(30) - 15;
                int zOffset = random.nextInt(30) - 15;

                Block highest = player.getWorld().getHighestBlockAt(
                        player.getLocation().getBlockX() + xOffset,
                        player.getLocation().getBlockZ() + zOffset
                );

                KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
                // INTEGRATION FIX: Do not build snow layers in protected regions!
                if (plugin.getProtectionManager().isProtected(highest.getLocation(), ProtectionType.BLOCK_DAMAGE)) {
                    return;
                }

                if (highest.getType().isSolid()) {
                    Block above = highest.getRelative(0, 1, 0);
                    if (above.getType() == Material.AIR) {
                        above.setType(Material.SNOW);
                    }
                }
            }
        }
    }
}