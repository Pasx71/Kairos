package com.kairos.seasons.effects;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.BiomeSeasonEffect;
import com.kairos.api.seasons.Season;
import com.kairos.handlers.protection.ProtectionType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WinterFreezeEffect implements BiomeSeasonEffect {

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
        long time = player.getWorld().getTime();
        if (player.getWorld().hasStorm() || (time > 13000 && time < 23000)) {
            
            KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
            FileConfiguration config = plugin.getConfigManager().getConfig("seasons/winter.yml");
            double freezeChance = config.getDouble("effects.water_freeze_chance", 0.25);
            
            if (random.nextDouble() < freezeChance) {
                int xOffset = random.nextInt(30) - 15;
                int zOffset = random.nextInt(30) - 15;

                Block highest = player.getWorld().getHighestBlockAt(
                        player.getLocation().getBlockX() + xOffset,
                        player.getLocation().getBlockZ() + zOffset
                );

                // INTEGRATION FIX: Do not freeze water in protected regions!
                if (plugin.getProtectionManager().isProtected(highest.getLocation(), ProtectionType.BLOCK_DAMAGE)) {
                    return;
                }

                if (highest.getType() == Material.WATER) {
                    if (highest.getBlockData() instanceof Levelled levelled && levelled.getLevel() == 0) {
                        highest.setType(Material.ICE);
                        player.getWorld().playSound(highest.getLocation(), Sound.BLOCK_GLASS_PLACE, 0.3f, 1.5f);
                    }
                }
            }
        }
    }
}