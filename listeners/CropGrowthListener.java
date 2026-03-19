package com.kairos.listeners;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.Season;
import com.kairos.handlers.protection.ProtectionType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.List;
import java.util.Random;

public class CropGrowthListener implements Listener {
    
    private final KairosPlugin plugin;
    private final Random random = new Random();

    public CropGrowthListener(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCropGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        Season season = plugin.getSeasonManager().getCurrentSeason(block.getWorld());
        Biome biome = block.getBiome();
        Material type = block.getType();

        FileConfiguration config = plugin.getConfigManager().getConfig("seasons/" + season.name().toLowerCase() + ".yml");
        
        // --- GRACEFUL INTEGRATION ---
        // Master toggle for farming mechanics! Let the server's existing plugin handle it if disabled.
        if (!config.getBoolean("farming.enabled", true)) return;

        int requiredLight = plugin.getConfigManager().getConfig("config.yml").getInt("farming.greenhouse_light_level", 10);
        boolean isGreenhouse = isProtectedGreenhouse(block, requiredLight);

        if (!isGreenhouse && plugin.getProtectionManager().isProtected(block.getLocation(), ProtectionType.CROP_DEATH)) {
            isGreenhouse = true; 
        }

        if (!isGreenhouse) {
            if (plugin.getBiomeGroupManager().isInGroup(biome, "wet")) {
            } else if (plugin.getBiomeGroupManager().isInGroup(biome, "hot")) {
                if (season == Season.SUMMER) { event.setCancelled(true); return; }
                if (season == Season.WINTER) season = Season.SPRING; 
            } else if (plugin.getBiomeGroupManager().isInGroup(biome, "cold")) {
                if (season != Season.SUMMER) { event.setCancelled(true); return; }
            } else {
                List<String> bannedCrops = config.getStringList("farming.banned_crops");
                if (bannedCrops.contains(type.name()) || bannedCrops.contains("ALL")) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        double penalty = config.getDouble("farming.growth_penalty", 0.0);
        if (penalty > 0 && random.nextDouble() < penalty) {
            if (!isGreenhouse || season == Season.WINTER) { 
                event.setCancelled(true);
                return;
            }
        }

        double boost = config.getDouble("farming.growth_boost", 0.0);
        if (boost > 0 && random.nextDouble() < boost) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> advanceCrop(block), 1L);
        }

        if (season == Season.SPRING) {
            boolean beeNearby = block.getWorld().getNearbyEntities(block.getLocation(), 15, 15, 15)
                    .stream().anyMatch(e -> e.getType() == EntityType.BEE);
                    
            double beeBoost = config.getDouble("farming.bee_growth_boost", 0.40);
            if (beeNearby && random.nextDouble() < beeBoost) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    advanceCrop(block);
                    block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3);
                }, 2L);
            }
        }
    }

    private boolean isProtectedGreenhouse(Block block, int requiredLight) {
        boolean skyBlocked = block.getWorld().getHighestBlockYAt(block.getLocation()) > block.getY();
        byte blockLight = block.getLightFromBlocks();
        return skyBlocked && blockLight >= requiredLight;
    }

    private void advanceCrop(Block block) {
        if (block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() < ageable.getMaximumAge()) {
                ageable.setAge(ageable.getAge() + 1);
                block.setBlockData(ageable);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        Season season = plugin.getSeasonManager().getCurrentSeason(event.getWorld());
        Biome biome = event.getLocation().getBlock().getBiome();
        FileConfiguration config = plugin.getConfigManager().getConfig("seasons/" + season.name().toLowerCase() + ".yml");
        
        // --- GRACEFUL INTEGRATION ---
        if (!config.getBoolean("farming.enabled", true)) return;

        double saplingDeathRate = config.getDouble("farming.sapling_death_rate", 0.60);

        if (season == Season.WINTER && !plugin.getBiomeGroupManager().isInGroup(biome, "hot") && !plugin.getBiomeGroupManager().isInGroup(biome, "wet")) {
            if (random.nextDouble() < saplingDeathRate) {
                event.setCancelled(true);
            }
        }
    }
}