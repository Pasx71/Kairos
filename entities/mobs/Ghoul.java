package com.kairos.entities.mobs;

import com.kairos.api.entities.AnimatedCustomEntity;
import com.kairos.api.entities.CustomEntity;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.utils.AnimationHandler;
import com.kairos.utils.AnimationHandler.BodyPart;
import com.kairos.utils.HeadUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import java.util.Random;

public class Ghoul extends AnimatedCustomEntity {

    public static final CustomEntityType TYPE = new CustomEntityType(
            "ghoul", "Ghoul", "&2&l", 30.0, EntityType.ZOMBIE) {
        @Override
        protected CustomEntity instantiateWrapper(LivingEntity entity) {
            return new Ghoul(entity);
        }
    };

    private boolean isCrawlingOut = true;
    private int crawlTicks; 

    public Ghoul(LivingEntity entity) {
        super(entity, TYPE);
        
        this.crawlTicks = getConfigInt("abilities.crawl_duration_ticks", 40);

        Random rand = new Random();
        ItemStack[] armor = {
            new ItemStack(Material.LEATHER_BOOTS), 
            new ItemStack(Material.LEATHER_LEGGINGS), 
            new ItemStack(Material.LEATHER_CHESTPLATE), 
            HeadUtils.ROTTEN_ZOMBIE.getHead()
        };
        
        int green = rand.nextInt(50) + 25;
        for (int i = 0; i < 3; i++) {
            LeatherArmorMeta meta = (LeatherArmorMeta) armor[i].getItemMeta();
            meta.setColor(Color.fromRGB(green - 15, green, 1)); 
            armor[i].setItemMeta(meta);
        }
        
        this.stand.getEquipment().setArmorContents(armor);
        this.entity.teleport(this.entity.getLocation().subtract(0, 1.5, 0));
        
        if (entity instanceof Mob mob) mob.setAware(false); 
    }

    @Override
    protected void setupAnimations() {
        this.walkAnimation = new AnimationHandler(true, true, true);
        this.walkAnimation.setAnimations(
            this.walkAnimation.new Animation(
                this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 0, 0, 0, -30, 0, 10, 12, false),
                this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 0, 0, 0, 30, 0, -10, 12, false),
                this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, 20, 0, 10, 12, false),
                this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, -20, 0, -10, 12, false)
            ),
            this.walkAnimation.new Animation(
                this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, -30, 0, 10, 0, 0, 0, 12, false),
                this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 30, 0, -10, 0, 0, 0, 12, false),
                this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 20, 0, 10, 0, 0, 0, 12, false),
                this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, -20, 0, -10, 0, 0, 0, 12, false)
            )
        );

        this.attackAnimation = new AnimationHandler(true, false, false);
        this.attackAnimation.setAnimations(
            this.attackAnimation.new Animation(
                this.attackAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, -110, 30, 15, 8, true),
                this.attackAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, -110, -30, -15, 8, true)
            )
        );
    }

    @Override
    public void tick() {
        if (entity == null || entity.isDead()) return;

        if (isCrawlingOut) {
            crawlTicks--;
            entity.teleport(entity.getLocation().add(0, 1.5 / getConfigInt("abilities.crawl_duration_ticks", 40), 0)); 
            
            entity.getWorld().spawnParticle(Particle.BLOCK_CRACK, entity.getLocation().add(0, 1.5, 0), 5, 0.4, 0.1, 0.4, Material.DIRT.createBlockData());
            if (crawlTicks % 5 == 0) {
                entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_ROOTED_DIRT_BREAK, 1.0f, 0.5f);
            }

            if (crawlTicks <= 0) {
                isCrawlingOut = false;
                if (entity instanceof Mob mob) mob.setAware(true);
            }
            return;
        }

        double grabDistance = getConfigDouble("abilities.grab_distance", 4.0);
        double grabStrength = getConfigDouble("abilities.grab_strength", 0.2);

        if (entity instanceof Mob mob && mob.getTarget() != null) {
            LivingEntity target = mob.getTarget();
            if (entity.getLocation().distanceSquared(target.getLocation()) < grabDistance) {
                
                // INTEGRATION FIX: Prevent grabbing players in safe zones
                EntityDamageByEntityEvent damageCheck = new EntityDamageByEntityEvent(
                        entity, target, EntityDamageEvent.DamageCause.CUSTOM, 0.0);
                Bukkit.getPluginManager().callEvent(damageCheck);
                
                if (!damageCheck.isCancelled()) {
                    playAttackAnimation();
                    Vector pull = entity.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(grabStrength);
                    target.setVelocity(target.getVelocity().add(pull));
                }
            }
        }
    }

    @Override
    protected void onDeath() {
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_DEATH, 1.0f, 0.5f);
        entity.getWorld().spawnParticle(Particle.SOUL, entity.getLocation().add(0, 0.5, 0), 15, 0.3, 0.5, 0.3, 0.03);
    }
}