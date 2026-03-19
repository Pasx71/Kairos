package com.kairos.seasons.effects;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.BiomeSeasonEffect;
import com.kairos.api.seasons.Season;
import com.kairos.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MudSlownessEffect implements BiomeSeasonEffect {

    private final Season season;

    public MudSlownessEffect(Season season) {
        this.season = season;
    }

    @Override
    public List<Biome> getBiomes() {
        List<Biome> wetBiomes = new ArrayList<>();
        for (Biome b : Biome.values()) {
            if (!b.name().contains("DESERT") && !b.name().contains("BADLANDS") && 
                !b.name().contains("NETHER") && !b.name().contains("END")) {
                wetBiomes.add(b);
            }
        }
        return wetBiomes;
    }

    @Override
    public Season getSeason() {
        return season;
    }

    @Override
    public void applyEffect(Player player) {
        KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
        boolean enabled = plugin.getConfig().getBoolean("seasons." + season.name().toLowerCase() + ".enable_mud_slowness", true);
        
        if (!enabled) return;

        if (player.getWorld().hasStorm()) {
            Material blockBelow = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
            
            if (blockBelow == Material.DIRT || blockBelow == Material.FARMLAND || 
                blockBelow == Material.COARSE_DIRT || blockBelow == Material.ROOTED_DIRT || 
                blockBelow == Material.PODZOL || blockBelow.name().endsWith("_PATH")) {
                
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0, false, false, false));
                
                if (Math.random() < 0.005) { 
                    MessageUtils.send(player, "messages.seasons.mud_slowness", "&7The muddy ground slows your movement...");
                }
            }
        }
    }
}
