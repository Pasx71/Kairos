package com.kairos.entities.mobs;

import com.kairos.api.entities.CustomEntity;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class BloodZombie extends CustomEntity {

    public static final CustomEntityType TYPE = new CustomEntityType(
            "blood_zombie", "Blood Zombie", "&4&l", 40.0, org.bukkit.entity.EntityType.ZOMBIE) {
        @Override
        protected CustomEntity instantiateWrapper(LivingEntity entity) {
            return new BloodZombie(entity);
        }
    };

    public BloodZombie(LivingEntity entity) {
        super(entity, TYPE);
        if (entity instanceof Zombie zombie) {
            zombie.setAdult(); 
        }
    }

    @Override
    public void tick() {
        int pullCooldown = getConfigInt("abilities.pull_cooldown_ticks", 60);
        double pullRadius = getConfigDouble("abilities.pull_radius", 8.0);
        double pullStrength = getConfigDouble("abilities.pull_strength", 0.8);

        if (ticksLived % 5 == 0) {
            Particle.DustOptions dust = new Particle.DustOptions(Color.RED, 1.5f);
            entity.getWorld().spawnParticle(Particle.REDSTONE, entity.getLocation().add(0, 1, 0), 3, 0.2, 0.5, 0.2, dust);
        }

        if (ticksLived % pullCooldown == 0 && entity instanceof Zombie zombie) {
            LivingEntity target = zombie.getTarget();
            if (target instanceof Player player && target.getLocation().distance(entity.getLocation()) < pullRadius) {
                
                // INTEGRATION FIX: Prevent pulling players who are in Invincible regions / Safe Zones
                EntityDamageByEntityEvent damageCheck = new EntityDamageByEntityEvent(
                        entity, player, EntityDamageEvent.DamageCause.CUSTOM, 0.0);
                Bukkit.getPluginManager().callEvent(damageCheck);
                
                if (!damageCheck.isCancelled()) {
                    Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                    player.setVelocity(direction.multiply(pullStrength).setY(0.2));
                    
                    MessageUtils.send(player, "messages.mobs.blood_zombie_pull", "&cThe Blood Zombie pulls you in!");
                }
            }
        }
    }

    @Override
    public void cleanUp() {
        Particle.DustOptions dust = new Particle.DustOptions(Color.MAROON, 3.0f);
        entity.getWorld().spawnParticle(Particle.REDSTONE, entity.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, dust);
    }
}