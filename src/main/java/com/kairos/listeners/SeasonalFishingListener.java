package com.kairos.listeners;

import com.kairos.KairosPlugin;
import com.kairos.api.items.CustomItem;
import com.kairos.api.seasons.Season;
import com.kairos.utils.PlaceholderUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SeasonalFishingListener implements Listener {

    private final KairosPlugin plugin;
    private final Random random = new Random();

    public SeasonalFishingListener(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    // NORMAL priority + ignoreCancelled allows us to work alongside other fishing plugins politely
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        Season season = plugin.getSeasonManager().getCurrentSeason(event.getPlayer().getWorld());
        FileConfiguration config = plugin.getConfigManager().getConfig("seasons/" + season.name().toLowerCase() + ".yml");

        if (!config.getBoolean("fishing.enabled", true)) return;

        if (event.getState() == PlayerFishEvent.State.FISHING) {
            double waitMult = config.getDouble("fishing.wait_multiplier", 1.0);
            event.getHook().setWaitTime((int) (event.getHook().getWaitTime() * waitMult));
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item caughtItem) {
            
            String mode = config.getString("fishing.mode", "REPLACE").toUpperCase();
            
            // GRACEFUL INTEGRATION: If replacing, don't overwrite custom items from other plugins
            if (mode.equals("REPLACE") && caughtItem.getItemStack().hasItemMeta() && caughtItem.getItemStack().getItemMeta().hasDisplayName()) {
                return; 
            }

            // DYNAMIC PLAYER MODIFIER (e.g. Karma, Luck, mcMMO Fishing Level)
            double baseChance = config.getDouble("fishing.custom_loot_chance", 0.0);
            String placeholder = config.getString("fishing.modifier.placeholder", "none");
            double scale = config.getDouble("fishing.modifier.scale", 0.0);
            
            // Math: Final Chance = Base + (Placeholder_Value * Scale)
            double modifierValue = PlaceholderUtils.parseDouble(event.getPlayer(), placeholder, 0.0);
            double finalChance = baseChance + (modifierValue * scale);

            if (finalChance > 0 && random.nextDouble() <= finalChance) {
                ConfigurationSection lootTable = config.getConfigurationSection("fishing.loot");
                
                if (lootTable != null) {
                    double totalWeight = 0;
                    for (String key : lootTable.getKeys(false)) {
                        totalWeight += lootTable.getDouble(key + ".weight", 1.0);
                    }

                    double randomRoll = random.nextDouble() * totalWeight;
                    double currentWeight = 0;

                    for (String key : lootTable.getKeys(false)) {
                        currentWeight += lootTable.getDouble(key + ".weight", 1.0);
                        
                        if (randomRoll <= currentWeight) {
                            String itemId = lootTable.getString(key + ".item", "DIRT");
                            int min = lootTable.getInt(key + ".min", 1);
                            int max = lootTable.getInt(key + ".max", 1);
                            int amount = min >= max ? min : min + random.nextInt((max - min) + 1);

                            ItemStack finalDrop = null;
                            CustomItem customItem = plugin.getItemRegistry().getItemById(itemId);
                            
                            if (customItem != null) {
                                finalDrop = customItem.buildItem();
                            } else {
                                Material material = Material.matchMaterial(itemId.toUpperCase());
                                if (material != null) finalDrop = new ItemStack(material);
                            }

                            if (finalDrop != null) {
                                finalDrop.setAmount(amount);

                                if (mode.equals("ADDITIVE")) {
                                    // Drop alongside the existing fish/loot!
                                    Item extraDrop = event.getPlayer().getWorld().dropItemNaturally(caughtItem.getLocation(), finalDrop);
                                    extraDrop.setVelocity(caughtItem.getVelocity()); // Make it fly to the player!
                                } else {
                                    // Replace the existing fish/loot
                                    caughtItem.setItemStack(finalDrop);
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
}