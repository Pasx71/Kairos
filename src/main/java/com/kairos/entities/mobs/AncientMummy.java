package com.kairos.entities.mobs;

import com.kairos.api.entities.CustomEntity;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.handlers.protection.ProtectionType;
import com.kairos.utils.HeadUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.java.JavaPlugin;

public class AncientMummy extends CustomEntity {

    public static final CustomEntityType TYPE = new CustomEntityType(
            "ancient_mummy", "Ancient Mummy", "&e&l", 40.0, EntityType.HUSK) {
        @Override
        protected CustomEntity instantiateWrapper(LivingEntity entity) {
            return new AncientMummy(entity);
        }
    };

    public AncientMummy(LivingEntity entity) {
        super(entity, TYPE);
        entity.getEquipment().setHelmet(HeadUtils.MUMMY.getHead());
        entity.getEquipment().setHelmetDropChance(0f);
        entity.setCanPickupItems(false);
        entity.setSilent(true);
    }

    @Override
    public void tick() {
        if (!(entity instanceof Mob mob)) return;

        int doorBreakTicks = getConfigInt("abilities.door_break_frequency", 10);
        double sprintSpeed = getConfigDouble("abilities.sprint_speed", 0.40);
        double normalSpeed = getConfigDouble("movement_speed", 0.25);

        if (ticksLived % 4 == 0) {
            entity.getWorld().spawnParticle(Particle.FALLING_DUST, entity.getLocation().add(0, 1.6, 0), 
                    4, 0.4, 0.5, 0.4, Material.OBSIDIAN.createBlockData());
        }

        if (Math.random() < 0.05 && ticksLived % 20 == 0) {
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_HUSK_AMBIENT, 1.0f, 0.7f);
        }

        // Break Wooden Doors
        if (ticksLived % doorBreakTicks == 0) {
            Block blockInFront = entity.getLocation().add(entity.getLocation().getDirection()).getBlock();
            if (blockInFront.getType().name().endsWith("_DOOR")) {
                
                // INTEGRATION FIX: Do not grief doors inside protected claims
                com.kairos.KairosPlugin plugin = JavaPlugin.getPlugin(com.kairos.KairosPlugin.class);
                if (!plugin.getProtectionManager().isProtected(blockInFront.getLocation(), ProtectionType.BLOCK_DAMAGE)) {
                    blockInFront.breakNaturally();
                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 1.0f);
                }
            }
        }

        if (mob.getTarget() != null && mob.getLocation().distanceSquared(mob.getTarget().getLocation()) > 25) {
            mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(sprintSpeed);
            if (ticksLived % 40 == 0) {
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 0.5f, 0.5f);
            }
        } else {
            mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(normalSpeed);
        }
    }

    @Override
    public void cleanUp() {
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_HUSK_DEATH, 1.0f, 0.6f);
    }
}