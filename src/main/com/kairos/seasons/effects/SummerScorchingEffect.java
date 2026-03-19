package com.kairos.seasons.effects;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.BiomeSeasonEffect;
import com.kairos.api.seasons.Season;
import com.kairos.utils.MessageUtils;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class SummerScorchingEffect implements BiomeSeasonEffect {

    @Override
    public List<Biome> getBiomes() {
        List<Biome> hotBiomes = new ArrayList<>();
        for (Biome b : Biome.values()) {
            if (b.name().contains("DESERT") || b.name().contains("BADLANDS") || b.name().contains("SAVANNA")) {
                hotBiomes.add(b);
            }
        }
        return hotBiomes;
    }

    @Override
    public Season getSeason() {
        return Season.SUMMER;
    }

    @Override
    public void applyEffect(Player player) {
        long time = player.getWorld().getTime();
        
        if (time > 2000 && time < 10000 && !player.getWorld().hasStorm()) {
            int highestBlock = player.getWorld().getHighestBlockYAt(player.getLocation());
            if (player.getLocation().getBlockY() >= highestBlock) {
                
                boolean hasHelmet = player.getEquipment() != null && player.getEquipment().getHelmet() != null;
                boolean hasHeavyArmor = false;
                
                if (player.getEquipment() != null) {
                    for (ItemStack item : player.getEquipment().getArmorContents()) {
                        if (item != null) {
                            String name = item.getType().name();
                            if (name.contains("IRON_") || name.contains("DIAMOND_") || name.contains("NETHERITE_") || name.contains("CHAINMAIL_")) {
                                hasHeavyArmor = true;
                                break;
                            }
                        }
                    }
                }
                
                if (!hasHelmet || hasHeavyArmor) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 100, 0, true, false, false));
                    
                    KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
                    double burnChance = plugin.getConfig().getDouble("seasons.summer.heavy_armor_burn_chance", 0.02);
                    
                    if (Math.random() < burnChance) {
                        player.setFireTicks(20); 
                        if (hasHeavyArmor && hasHelmet) {
                            MessageUtils.send(player, "messages.seasons.heavy_armor_heat", "&cYour heavy armor is cooking you alive in the summer heat!");
                        }
                    }
                }
            }
        }
    }
}
