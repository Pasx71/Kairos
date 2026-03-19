package com.kairos.seasons.effects;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.BiomeSeasonEffect;
import com.kairos.api.seasons.Season;
import com.kairos.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SummerSandEffect implements BiomeSeasonEffect {

    @Override
    public List<Biome> getBiomes() {
        return JavaPlugin.getPlugin(KairosPlugin.class).getBiomeGroupManager().getBiomesInGroup("hot");
    }

    @Override
    public Season getSeason() { return Season.SUMMER; }

    @Override
    public void applyEffect(Player player) {
        long time = player.getWorld().getTime();
        
        if (time > 2000 && time < 10000 && !player.getWorld().hasStorm()) {
            if (player.getEquipment() == null || player.getEquipment().getBoots() == null) {
                
                Material blockBelow = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
                if (blockBelow == Material.SAND || blockBelow == Material.RED_SAND) {
                    
                    KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
                    FileConfiguration config = plugin.getConfigManager().getConfig("seasons/summer.yml");
                    
                    double burnChance = config.getDouble("effects.hot_sand_burn_chance", 0.05);
                    double burnDamage = config.getDouble("effects.hot_sand_damage", 1.0);
                    
                    if (Math.random() < burnChance) {
                        player.damage(burnDamage);
                        player.setFireTicks(20);
                        MessageUtils.send(player, "messages.seasons.hot_sand", "&cThe scorching sand burns your feet! You should wear boots!");
                    }
                }
            }
        }
    }
}
