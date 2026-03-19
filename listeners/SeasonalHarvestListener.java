package com.kairos.listeners;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.Season;
import com.kairos.utils.PlaceholderUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class SeasonalHarvestListener implements Listener {

    private final KairosPlugin plugin;

    public SeasonalHarvestListener(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCropHarvest(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        if (block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                
                Season season = plugin.getSeasonManager().getCurrentSeason(block.getWorld());
                FileConfiguration config = plugin.getConfigManager().getConfig("seasons/" + season.name().toLowerCase() + ".yml");
                
                // --- GRACEFUL INTEGRATION ---
                // If farming mechanics are disabled, skip double harvests too.
                if (!config.getBoolean("farming.enabled", true)) return;

                if (season == Season.AUTUMN && config.getBoolean("farming.enable_double_harvest", false)) {
                    
                    // DYNAMIC PLAYER MODIFIER (e.g. Farming Skill, Karma)
                    double baseChance = config.getDouble("farming.double_harvest_chance", 1.0);
                    String placeholder = config.getString("farming.modifier.placeholder", "none");
                    double scale = config.getDouble("farming.modifier.scale", 0.0);
                    
                    double modifierValue = PlaceholderUtils.parseDouble(event.getPlayer(), placeholder, 0.0);
                    double finalChance = baseChance + (modifierValue * scale);

                    if (finalChance > 0 && Math.random() <= finalChance) {
                        Collection<ItemStack> drops = block.getDrops(event.getPlayer().getInventory().getItemInMainHand());
                        
                        // Additive vs Replace is inherently additive here, as we are adding more drops.
                        for (ItemStack drop : drops) {
                            block.getWorld().dropItemNaturally(block.getLocation(), drop);
                        }
                        
                        block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3);
                        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
                    }
                }
            }
        }
    }
}