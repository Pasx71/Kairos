package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.Season;
import com.kairos.utils.PlaceholderUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class FoodSpoilageTask implements Runnable {

    private final KairosPlugin plugin;
    private final Random random = new Random();
    
    // The items that are susceptible to spoiling
    private final Set<Material> RAW_FOODS = EnumSet.of(
            Material.BEEF, Material.PORKCHOP, Material.CHICKEN, 
            Material.MUTTON, Material.RABBIT, Material.COD, Material.SALMON
    );

    public FoodSpoilageTask(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Season season = plugin.getSeasonManager().getCurrentSeason(player.getWorld());
            FileConfiguration config = plugin.getConfigManager().getConfig("seasons/" + season.name().toLowerCase() + ".yml");

            if (!config.getBoolean("spoilage.enabled", false)) continue;

            double baseSpoilChance = config.getDouble("spoilage.chance_per_interval", 0.05);
            
            // Dynamic integration with skills/stats
            String placeholder = config.getString("spoilage.modifier.placeholder", "none");
            double scale = config.getDouble("spoilage.modifier.scale", 0.0);
            double resistance = PlaceholderUtils.parseDouble(player, placeholder, 0.0);
            
            double finalChance = baseSpoilChance - (resistance * scale);

            if (finalChance <= 0) continue;

            // Iterate through player's inventory
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                
                if (item != null && RAW_FOODS.contains(item.getType())) {
                    if (random.nextDouble() < finalChance) {
                        
                        // Rot 1 piece of the stack
                        item.setAmount(item.getAmount() - 1);
                        player.getInventory().addItem(new ItemStack(Material.ROTTEN_FLESH, 1));
                        
                        // Small gross sound effect
                        if (random.nextDouble() < 0.1) {
                            player.playSound(player.getLocation(), Sound.ENTITY_SLIME_SQUISH, 0.5f, 0.5f);
                        }
                    }
                }
            }
        }
    }
}