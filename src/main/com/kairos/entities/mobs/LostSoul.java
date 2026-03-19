package com.kairos.entities.mobs;

import com.kairos.api.entities.CustomEntity;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.utils.HeadUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

import java.util.Random;

public class LostSoul extends CustomEntity {

    public static final CustomEntityType TYPE = new CustomEntityType(
            "lost_soul", "Lost Soul", "&3&l", 14.0, EntityType.ZOMBIE) {
        @Override
        protected CustomEntity instantiateWrapper(LivingEntity entity) {
            return new LostSoul(entity);
        }
    };

    public LostSoul(LivingEntity entity) {
        super(entity, TYPE);
        if (entity instanceof Zombie zombie) {
            zombie.setBaby(); 
            zombie.setInvisible(true);
            zombie.setSilent(true);
            
            HeadUtils[] heads = {HeadUtils.SOUL_1, HeadUtils.SOUL_2, HeadUtils.SOUL_3, HeadUtils.SOUL_4};
            zombie.getEquipment().setHelmet(heads[new Random().nextInt(heads.length)].getHead());
            zombie.getEquipment().setHelmetDropChance(0f);
        }
    }

    @Override
    public void tick() {
        if (ticksLived % 4 == 0) {
            entity.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, entity.getLocation().add(0, 0.5, 0), 1, 0.2, 0.2, 0.2, 0.05);
            entity.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, entity.getLocation().add(0, 0.5, 0), 1, 0.1, 0.1, 0.1, 0.01);
        }

        if (ticksLived % 40 == 0 && Math.random() < 0.2) {
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_VEX_AMBIENT, 1.0f, 0.5f);
        }
    }

    @Override
    public void cleanUp() {
        entity.getWorld().spawnParticle(Particle.SOUL, entity.getLocation().add(0, 0.5, 0), 15, 0.3, 0.5, 0.3, 0.03);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_VEX_DEATH, 1.0f, 0.7f);
    }
}