package com.kairos.listeners;

import com.kairos.KairosPlugin;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.api.seasons.Season;
import com.kairos.handlers.protection.ProtectionType;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class SeasonalMobSpawnListener implements Listener {

    private final KairosPlugin plugin;
    private final Random random = new Random();
    private final NamespacedKey customEntityKey;

    public SeasonalMobSpawnListener(KairosPlugin plugin) {
        this.plugin = plugin;
        this.customEntityKey = new NamespacedKey(plugin, "kairos_custom_entity");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBeeSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.BEE) {
            Season season = plugin.getSeasonManager().getCurrentSeason(event.getLocation().getWorld());
            FileConfiguration config = plugin.getConfigManager().getConfig("seasons/" + season.name().toLowerCase() + ".yml");
            
            boolean hibernate = config.getBoolean("spawning.hibernate_bees", true);
            Biome biome = event.getLocation().getBlock().getBiome();
            
            if (hibernate && season == Season.WINTER && !plugin.getBiomeGroupManager().isInGroup(biome, "wet")) {
                if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BEEHIVE) {
                    event.setCancelled(true);
                } else {
                    event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10, false, false, false));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL && 
            event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CHUNK_GEN) {
            return;
        }

        if (plugin.getProtectionManager().isProtected(event.getLocation(), ProtectionType.MOB_SPAWN)) return; 
        if (event.getEntity().getPersistentDataContainer().has(customEntityKey, PersistentDataType.STRING)) return;

        Season season = plugin.getSeasonManager().getCurrentSeason(event.getLocation().getWorld());
        FileConfiguration config = plugin.getConfigManager().getConfig("seasons/" + season.name().toLowerCase() + ".yml");
        
        ConfigurationSection replacements = config.getConfigurationSection("spawning.mob_replacements." + event.getEntityType().name());
        
        if (replacements != null) {
            String mode = config.getString("spawning.mode", "REPLACE").toUpperCase();
            
            double roll = random.nextDouble();
            double currentChance = 0.0;

            for (String replacementKey : replacements.getKeys(false)) {
                double chance = replacements.getDouble(replacementKey);
                currentChance += chance;

                if (roll <= currentChance) {
                    if (mode.equals("REPLACE")) {
                        event.setCancelled(true);
                    }
                    
                    CustomEntityType customType = CustomEntityType.getCustomEntityType(replacementKey);
                    if (customType != null) {
                        customType.spawn(event.getLocation());
                    } else {
                        try {
                            EntityType vanillaType = EntityType.valueOf(replacementKey.toUpperCase());
                            event.getLocation().getWorld().spawnEntity(event.getLocation(), vanillaType);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid mob replacement: " + replacementKey);
                        }
                    }
                    return; 
                }
            }
        }

        if (season == Season.WINTER && config.getBoolean("spawning.cancel_spiders", true)) {
            if (event.getEntityType() == EntityType.SPIDER || event.getEntityType() == EntityType.CAVE_SPIDER) {
                event.setCancelled(true);
            }
        }
    }
}