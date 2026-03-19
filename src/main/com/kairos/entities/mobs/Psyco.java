package com.kairos.entities.mobs;

import com.kairos.api.entities.CustomEntity;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.handlers.protection.ProtectionType;
import com.kairos.utils.HeadUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Psyco extends CustomEntity {

    public static final CustomEntityType TYPE = new CustomEntityType(
            "psyco", "Psyco", "&6&l", 40.0, EntityType.HUSK) {
        @Override
        protected CustomEntity instantiateWrapper(LivingEntity entity) {
            return new Psyco(entity);
        }
    };

    private int teleportCooldown = 0;

    public Psyco(LivingEntity entity) {
        super(entity, TYPE);
        
        ItemStack[] armor = {
            new ItemStack(Material.LEATHER_BOOTS), 
            new ItemStack(Material.LEATHER_LEGGINGS), 
            new ItemStack(Material.LEATHER_CHESTPLATE), 
            HeadUtils.STALKER.getHead()
        };
        
        for (int i = 0; i < 3; i++) {
            LeatherArmorMeta meta = (LeatherArmorMeta) armor[i].getItemMeta();
            meta.setColor(Color.fromRGB(105, 68, 31)); 
            armor[i].setItemMeta(meta);
        }
        
        entity.getEquipment().setArmorContents(armor);
        entity.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
    }

    @Override
    public void tick() {
        if (!(entity instanceof Mob mob)) return;

        int doorBreakFrequency = getConfigInt("abilities.door_break_frequency", 10);
        int maxTpCooldown = getConfigInt("abilities.teleport_cooldown_ticks", 60);
        double minStalkRange = Math.pow(getConfigDouble("abilities.min_stalk_range", 8.0), 2);
        double maxStalkRange = Math.pow(getConfigDouble("abilities.max_stalk_range", 40.0), 2);

        // Door Breaking
        if (ticksLived % doorBreakFrequency == 0) {
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

        if (teleportCooldown > 0) {
            teleportCooldown--;
            return;
        }

        // Teleport Logic
        if (mob.getTarget() != null) {
            LivingEntity target = mob.getTarget();
            double dist = entity.getLocation().distanceSquared(target.getLocation());
            
            if (dist > minStalkRange && dist < maxStalkRange) {
                Vector toTarget = target.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                Vector targetLooking = target.getLocation().getDirection();

                if (toTarget.dot(targetLooking) > 0.5) {
                    teleportBehind(target, maxTpCooldown);
                }
            }
        }
    }

    private void teleportBehind(LivingEntity target, int maxTpCooldown) {
        Location behind = target.getLocation().subtract(target.getLocation().getDirection().multiply(2));
        behind.setY(target.getWorld().getHighestBlockYAt(behind) + 1);

        entity.getWorld().spawnParticle(Particle.PORTAL, entity.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        
        entity.teleport(behind);
        
        entity.getWorld().spawnParticle(Particle.PORTAL, entity.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        
        teleportCooldown = maxTpCooldown;
    }

    @Override
    public void cleanUp() {
        entity.getWorld().spawnParticle(Particle.SOUL, entity.getLocation().add(0, 1.5, 0), 15, 0.3, 0.3, 0.3, 0.01);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_HUSK_DEATH, 1.0f, 0.6f);
    }
}