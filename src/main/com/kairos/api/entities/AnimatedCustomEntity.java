package com.kairos.api.entities;

import com.kairos.utils.AnimationHandler;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.EulerAngle;

public abstract class AnimatedCustomEntity extends CustomEntity {

    protected ArmorStand stand;
    protected Location stepLocation;
    protected AnimationHandler walkAnimation;
    protected AnimationHandler attackAnimation;
    
    protected float bodyRotation;
    protected float rotationSpeed = 8.0f;
    protected boolean shouldWalk = true;

    public AnimatedCustomEntity(LivingEntity entity, CustomEntityType type) {
        super(entity, type);
        this.stepLocation = entity.getLocation();
        
        // Spawn the invisible ArmorStand that will act as the "bones" of the model
        this.stand = (ArmorStand) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ARMOR_STAND);
        this.stand.setVisible(false);
        this.stand.setGravity(false);
        this.stand.setMarker(true); // Prevents hitboxes from blocking player swings
        
        // Make the vanilla mob invisible so we only see the armor stand
        this.entity.setInvisible(true);

        setupAnimations();
    }

    /** Define the walkAnimation and attackAnimation here */
    protected abstract void setupAnimations();

    @Override
    public void baseTick() {
        super.baseTick(); // Runs normal tick updates

        if (stand == null || !stand.isValid()) return;

        // Sync ArmorStand position with the hidden Vanilla Mob
        Location entityLoc = entity.getLocation();
        float rot = entityLoc.getYaw() - bodyRotation;
        
        if (Math.abs(rot) > 300) {
            bodyRotation = entityLoc.getYaw();
        } else {
            bodyRotation += (rot) / rotationSpeed;
        }
        
        stand.setHeadPose(new EulerAngle(Math.toRadians(entity.getLocation().getPitch()), Math.toRadians(entityLoc.getYaw() - bodyRotation), 0));
        entityLoc.setYaw(bodyRotation);
        
        // We use teleport to move the stand smoothly with the mob
        stand.teleport(entityLoc);
        
        // Handle Walking Animation Trigger
        if (shouldWalk && stepLocation.distanceSquared(entity.getLocation()) > 0.01 && entity.isOnGround()) {
            stepLocation = entity.getLocation();
            if (walkAnimation != null) walkAnimation.go();
        } else {
            if (walkAnimation != null) walkAnimation.stop();
        }
        
        if (walkAnimation != null) walkAnimation.tick(stand);
        if (attackAnimation != null) attackAnimation.tick(stand);
    }

    public void playAttackAnimation() {
        if (stand != null && attackAnimation != null) {
            attackAnimation.startAnimation(stand);
        }
    }

    @Override
    public void cleanUp() {
        if (stand != null && stand.isValid()) {
            stand.remove();
        }
        onDeath();
    }

    /** Cleanly fires when the entity dies or is deleted */
    protected abstract void onDeath();
}