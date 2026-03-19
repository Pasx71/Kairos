package com.kairos.entities.mobs;

import com.kairos.api.entities.AnimatedCustomEntity;
import com.kairos.api.entities.CustomEntity;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.utils.AnimationHandler;
import com.kairos.utils.AnimationHandler.BodyPart;
import com.kairos.utils.HeadUtils;
import com.kairos.utils.RepeatingTask;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Scarecrow extends AnimatedCustomEntity {

    public static final CustomEntityType TYPE = new CustomEntityType(
            "scarecrow", "Scarecrow", "&6&l", 30.0, EntityType.ZOMBIE) {
        @Override
        protected CustomEntity instantiateWrapper(LivingEntity entity) {
            return new Scarecrow(entity);
        }
    };

    private int attackCooldown = 0;

    public Scarecrow(LivingEntity entity) {
        super(entity, TYPE);

        ItemStack[] armor = {
            new ItemStack(Material.LEATHER_BOOTS), 
            new ItemStack(Material.LEATHER_LEGGINGS), 
            new ItemStack(Material.LEATHER_CHESTPLATE), 
            HeadUtils.SCARECROW.getHead()
        };
        
        for (int i = 0; i < 3; i++) {
            LeatherArmorMeta meta = (LeatherArmorMeta) armor[i].getItemMeta();
            meta.setColor(Color.fromRGB(105, 68, 31)); 
            armor[i].setItemMeta(meta);
        }
        
        this.stand.getEquipment().setArmorContents(armor);
        this.stand.getEquipment().setItemInMainHand(new ItemStack(Material.JACK_O_LANTERN));
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
                this.attackAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, -120, 0, 15, 8, true)
            )
        );
    }

    @Override
    public void tick() {
        if (entity == null || entity.isDead()) return;

        double throwRange = getConfigDouble("abilities.throw_range", 15.0);
        int maxCooldown = getConfigInt("abilities.throw_cooldown_ticks", 60);

        if (ticksLived % 5 == 0) {
            entity.getWorld().spawnParticle(Particle.FALLING_DUST, entity.getLocation().add(0, 1.1, 0), 1, 0.2, 0.4, 0.2, Material.JACK_O_LANTERN.createBlockData());
        }

        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        if (entity instanceof Mob mob && mob.getTarget() != null) {
            LivingEntity target = mob.getTarget();
            double dist = entity.getLocation().distanceSquared(target.getLocation());
            
            if (dist > 16 && dist < (throwRange * throwRange)) {
                attackCooldown = maxCooldown;
                playAttackAnimation();
                throwPumpkin(target);
            }
        }
    }

    private void throwPumpkin(LivingEntity target) {
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.5f);
        this.stand.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));

        Location spawnLoc = entity.getEyeLocation().add(entity.getLocation().getDirection());
        ArmorStand projectile = (ArmorStand) entity.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        
        projectile.setVisible(false);
        projectile.setMarker(true);
        projectile.setGravity(false);
        
        // TRIPLE-CHECK FIX: Ensure the armor stand does not leak memory if the chunk unloads
        projectile.setPersistent(false); 
        
        projectile.getEquipment().setHelmet(new ItemStack(Material.JACK_O_LANTERN));
        
        Vector direction = target.getEyeLocation().toVector().subtract(spawnLoc.toVector()).normalize().multiply(1.2);
        com.kairos.KairosPlugin plugin = JavaPlugin.getPlugin(com.kairos.KairosPlugin.class);
        
        double splashDamage = getConfigDouble("abilities.splash_damage", 6.0);
        
        new RepeatingTask(plugin, 0, 1) {
            int life = 40; 
            @Override
            public void run() {
                if (life-- <= 0 || projectile.isDead() || entity.isDead()) {
                    explode(projectile, splashDamage);
                    cancel();
                    return;
                }

                Location nextLoc = projectile.getLocation().add(direction);
                projectile.teleport(nextLoc);
                projectile.setHeadPose(projectile.getHeadPose().add(0.3, 0, 0)); 

                if (nextLoc.getBlock().getType().isSolid()) {
                    explode(projectile, splashDamage);
                    cancel();
                    return;
                }
                
                for (org.bukkit.entity.Entity e : projectile.getNearbyEntities(0.6, 0.6, 0.6)) {
                    if (e instanceof LivingEntity && e != entity && e != stand) {
                        explode(projectile, splashDamage);
                        cancel();
                        return;
                    }
                }
            }
        };

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (stand != null && stand.isValid()) {
                stand.getEquipment().setItemInMainHand(new ItemStack(Material.JACK_O_LANTERN));
            }
        }, 20L);
    }

    private void explode(ArmorStand projectile, double splashDamage) {
        Location loc = projectile.getLocation();
        projectile.remove();
        
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 30, 0.5, 0.5, 0.5, Material.JACK_O_LANTERN.createBlockData());
        
        for (org.bukkit.entity.Entity e : loc.getWorld().getNearbyEntities(loc, 3, 3, 3)) {
            if (e instanceof LivingEntity target && target != entity && target != stand) {
                
                // INTEGRATION FIX: Mock damage event so we don't apply fire ticks in safe zones
                EntityDamageByEntityEvent damageCheck = new EntityDamageByEntityEvent(
                        entity, target, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, 0.0);
                Bukkit.getPluginManager().callEvent(damageCheck);
                
                if (!damageCheck.isCancelled()) {
                    target.damage(splashDamage, entity);
                    target.setFireTicks(60); 
                }
            }
        }
    }

    @Override
    protected void onDeath() {
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_WITHER_SKELETON_DEATH, 1.0f, 0.5f);
        entity.getWorld().spawnParticle(Particle.SOUL, entity.getLocation().add(0, 0.5, 0), 15, 0.3, 0.5, 0.3, 0.03);
    }
}