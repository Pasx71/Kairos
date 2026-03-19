package com.kairos.seasons.effects;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.BiomeSeasonEffect;
import com.kairos.api.seasons.Season;
import com.kairos.handlers.protection.ProtectionType;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SpringThawEffect implements BiomeSeasonEffect {

    private final Random random = new Random();

    @Override
    public List<Biome> getBiomes() {
        KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
        List<Biome> biomes = new ArrayList<>(Arrays.asList(Biome.values()));
        
        biomes.removeAll(plugin.getBiomeGroupManager().getBiomesInGroup("cold"));
        
        return biomes;
    }

    @Override
    public Season getSeason() { return Season.SPRING; }

    @Override
    public void applyEffect(Player player) {
        long time = player.getWorld().getTime();
        if (time > 0 && time < 12000 && !player.getWorld().hasStorm()) {
            if (random.nextDouble() < 0.15) {
                int xOffset = random.nextInt(30) - 15;
                int zOffset = random.nextInt(30) - 15;

                Block highest = player.getWorld().getHighestBlockAt(
                        player.getLocation().getBlockX() + xOffset,
                        player.getLocation().getBlockZ() + zOffset
                );

                KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
                // INTEGRATION FIX: Do not melt ice/snow inside protected claims!
                if (plugin.getProtectionManager().isProtected(highest.getLocation(), ProtectionType.BLOCK_DAMAGE)) {
                    return;
                }

                if (highest.getType() == Material.SNOW) {
                    highest.setType(Material.AIR);
                } else if (highest.getType() == Material.ICE) {
                    highest.setType(Material.WATER);
                }
            }
        }
    }
}