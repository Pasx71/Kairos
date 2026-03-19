package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.api.disease.Disease;
import com.kairos.utils.ColorUtils;
import com.kairos.utils.PlaceholderUtils;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Random;

public class DiseaseTask implements Runnable {

    private final DiseaseManager manager;
    private final Random random = new Random();

    public DiseaseTask(DiseaseManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        if (!manager.isEnabled()) return;

        for (World world : org.bukkit.Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                
                List<Disease> activeDiseases = manager.getActiveDiseases(entity);
                
                // Process Outbreaks for healthy animals
                if (activeDiseases.isEmpty() && !(entity instanceof Player)) {
                    for (Disease disease : manager.getAllDiseases()) {
                        if (disease.isOutbreakEnabled() && disease.getHosts().contains(entity.getType())) {
                            checkForOutbreak(entity, disease);
                        }
                    }
                } 
                // Process infected entities
                else {
                    for (Disease disease : activeDiseases) {
                        processInfectedEntity(entity, disease);
                    }
                }
            }
        }
    }

    private void processInfectedEntity(LivingEntity entity, Disease disease) {
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(100, 150, 50), 1.5f);
        entity.getWorld().spawnParticle(Particle.REDSTONE, entity.getLocation().add(0, 1, 0), 5, 0.5, 0.5, 0.5, dust);
        
        if (entity instanceof Player player) {
            if (random.nextDouble() < 0.2) {
                player.sendMessage(ColorUtils.colorize(disease.getSymptomMsg()));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_DROWN, 0.5f, 0.8f);
            }
        } else if (random.nextDouble() < 0.2) {
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 0.5f, 1.5f);
        }

        for (PotionEffect effect : disease.getEffects()) {
            entity.addPotionEffect(effect);
        }
        
        if (disease.getDamagePerTick() > 0) {
            entity.damage(disease.getDamagePerTick());
        }

        // Proximity Spreading
        if (disease.isProximityEnabled()) {
            double r = disease.getProximityRadius();
            String currentSeason = JavaPlugin.getPlugin(KairosPlugin.class).getSeasonManager().getCurrentSeason(entity.getWorld()).name();

            for (Entity near : entity.getNearbyEntities(r, r, r)) {
                if (near instanceof LivingEntity target) {
                    if (disease.getHosts().contains(target.getType()) && !manager.hasDisease(target, disease)) {
                        
                        double finalChance = disease.getProximityChance();

                        if (target instanceof Player p) {
                            FileConfiguration config = JavaPlugin.getPlugin(KairosPlugin.class).getConfigManager().getConfig("diseases.yml");
                            String placeholder = config.getString("integration.resistance.placeholder", "none");
                            double scale = config.getDouble("integration.resistance.scale", 0.0);
                            
                            double resistance = PlaceholderUtils.parseDouble(p, placeholder, 0.0);
                            finalChance -= (resistance * scale);
                        }

                        if (finalChance > 0 && random.nextDouble() < finalChance) {
                            if (disease.canContractInSeason(currentSeason)) {
                                manager.infect(target, disease);
                                if (target instanceof Player p) {
                                    p.sendMessage(ColorUtils.colorize(disease.getContractedMsg()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkForOutbreak(LivingEntity entity, Disease disease) {
        if (random.nextDouble() > disease.getOutbreakChance()) return;

        double r = 4.0;
        long nearbyCount = entity.getNearbyEntities(r, r, r).stream()
                .filter(e -> e.getType() == entity.getType())
                .count();

        if (nearbyCount >= disease.getOutbreakCrowdThreshold()) {
            String currentSeason = JavaPlugin.getPlugin(KairosPlugin.class).getSeasonManager().getCurrentSeason(entity.getWorld()).name();
            if (disease.canContractInSeason(currentSeason)) {
                manager.infect(entity, disease);
            }
        }
    }
}