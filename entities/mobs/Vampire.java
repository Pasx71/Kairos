package com.kairos.entities.mobs;

import com.kairos.api.entities.CustomEntity;
import com.kairos.api.entities.CustomEntityType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

public class Vampire extends CustomEntity {

    public static final CustomEntityType TYPE = new CustomEntityType(
            "vampire", "Vampire", "&c&l", 40.0, EntityType.ZOMBIE) {
        @Override
        protected CustomEntity instantiateWrapper(LivingEntity entity) {
            return new Vampire(entity);
        }
    };

    public Vampire(LivingEntity entity) {
        super(entity, TYPE);
        if (entity instanceof Zombie zombie) {
            zombie.setAdult();
            zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        }
    }

    @Override
    public void tick() {
        double currentHealth = entity.getHealth();
        double maxHealth = TYPE.maxHealth;

        // Fetch dynamic abilities
        double morphToBatThreshold = getConfigDouble("abilities.morph_to_bat_threshold", 0.30);
        double morphToZombieThreshold = getConfigDouble("abilities.morph_to_zombie_threshold", 0.60);
        double healAmount = getConfigDouble("abilities.heal_amount", 1.0);
        int healFrequency = getConfigInt("abilities.heal_frequency_ticks", 10);

        if (entity.getType() == EntityType.ZOMBIE) {
            if (currentHealth < maxHealth * morphToBatThreshold) {
                playMorphEffect();
                Bat bat = (Bat) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.BAT);
                morph(bat); 
            }
        } 
        else if (entity.getType() == EntityType.BAT) {
            entity.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, entity.getLocation(), 1, 0.2, 0.2, 0.2, 0);
            
            if (ticksLived % healFrequency == 0) {
                entity.setHealth(Math.min(maxHealth, currentHealth + healAmount)); 
            }
            
            if (currentHealth > maxHealth * morphToZombieThreshold) {
                playMorphEffect();
                Zombie zombie = (Zombie) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ZOMBIE);
                zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                morph(zombie); 
            }
        }
    }

    private void playMorphEffect() {
        entity.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, entity.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.05);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 0.5f);
    }

    @Override
    public void cleanUp() {
        playMorphEffect();
    }
}