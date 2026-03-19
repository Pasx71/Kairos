package com.kairos.entities.mobs;

import com.kairos.api.entities.CustomEntity;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.utils.HeadUtils;
import com.kairos.utils.RepeatingTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class AncientSkeleton extends CustomEntity {

    public static final CustomEntityType TYPE = new CustomEntityType(
            "ancient_skeleton", "Ancient Skeleton", "&e&l", 30.0, EntityType.SKELETON) {
        @Override
        protected CustomEntity instantiateWrapper(LivingEntity entity) {
            return new AncientSkeleton(entity);
        }
    };

    private int attackCooldown = 0;

    public AncientSkeleton(LivingEntity entity) {
        super(entity, TYPE);
        entity.getEquipment().setHelmet(HeadUtils.ANCIENT_SKELETON.getHead());
        entity.getEquipment().setItemInMainHand(new ItemStack(Material.AIR)); // Empty hand for spellcasting
    }

    @Override
    public void tick() {
        if (!(entity instanceof Mob mob)) return;

        double attackRange = getConfigDouble("abilities.attack_range", 15.0);
        int maxCooldown = getConfigInt("abilities.attack_cooldown_ticks", 80);

        entity.getWorld().spawnParticle(Particle.FLAME, entity.getLocation().add(0, 1.3, 0), 1, 0.25, 0.45, 0.25, 0.01);

        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        if (mob.getTarget() != null && mob.getLocation().distanceSquared(mob.getTarget().getLocation()) < (attackRange * attackRange)) {
            attackCooldown = maxCooldown;
            shootSpell(mob.getTarget());
        }
    }

    private void shootSpell(LivingEntity target) {
        entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.6f);
        
        Location spellLoc = entity.getEyeLocation().add(entity.getLocation().getDirection());
        Vector direction = target.getEyeLocation().toVector().subtract(spellLoc.toVector()).normalize().multiply(0.7);

        com.kairos.KairosPlugin plugin = JavaPlugin.getPlugin(com.kairos.KairosPlugin.class);
        double spellDamage = getConfigDouble("abilities.spell_damage", 5.0);
        
        new RepeatingTask(plugin, 0, 1) {
            int life = 30;
            @Override
            public void run() {
                if (life-- <= 0 || entity.isDead()) {
                    cancel();
                    return;
                }

                spellLoc.add(direction);
                spellLoc.getWorld().spawnParticle(Particle.FLAME, spellLoc, 5, 0.2, 0.2, 0.2, 0.05);
                spellLoc.getWorld().spawnParticle(Particle.BLOCK_DUST, spellLoc, 5, 0.2, 0.2, 0.2, Material.SAND.createBlockData());

                for (org.bukkit.entity.Entity e : spellLoc.getWorld().getNearbyEntities(spellLoc, 1.0, 1.0, 1.0)) {
                    if (e instanceof LivingEntity hit && hit != entity) {
                        hit.setFireTicks(80);
                        hit.damage(spellDamage, entity);
                        cancel();
                        return;
                    }
                }
            }
        };
    }

    @Override
    public void cleanUp() {
        entity.getWorld().spawnParticle(Particle.FLAME, entity.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.2);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_SKELETON_DEATH, 1.0f, 0.5f);
    }
}