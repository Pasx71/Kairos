package com.kairos.seasons.effects;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.BiomeSeasonEffect;
import com.kairos.api.seasons.Season;
import com.kairos.utils.MessageUtils;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class WinterDesertEffect implements BiomeSeasonEffect {

    @Override
    public List<Biome> getBiomes() {
        return JavaPlugin.getPlugin(KairosPlugin.class).getBiomeGroupManager().getBiomesInGroup("hot");
    }

    @Override
    public Season getSeason() { return Season.WINTER; }

    @Override
    public void applyEffect(Player player) {
        long time = player.getWorld().getTime();
        
        // At night in the desert/savannas during winter...
        if (time > 13000 && time < 23000) { 
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0, false, false, false));
            
            if (Math.random() < 0.05) {
                MessageUtils.send(player, "messages.seasons.desert_night_chill", "&bThe winter desert winds chill you to the bone...");
            }
        }
    }
}
