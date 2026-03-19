package com.kairos.entities.mobs;

import com.kairos.api.entities.CustomEntity;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.utils.HeadUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.inventory.ItemStack;

public class SkeletonKnight extends CustomEntity {

    public static final CustomEntityType TYPE = new CustomEntityType(
            "skeleton_knight", "Skeleton Knight", "&8&l", 20.0, EntityType.SKELETON) {
        @Override
        protected CustomEntity instantiateWrapper(LivingEntity entity) {
            return new SkeletonKnight(entity);
        }
    };

    private SkeletonHorse horse;

    public SkeletonKnight(LivingEntity entity) {
        super(entity, TYPE);
        
        entity.getEquipment().setHelmet(HeadUtils.SKELETON_KNIGHT.getHead());
        entity.getEquipment().setHelmetDropChance(0f);
        entity.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        entity.getEquipment().setItemInOffHand(new ItemStack(Material.SHIELD));

        if (!entity.isInsideVehicle()) {
            this.horse = (SkeletonHorse) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.SKELETON_HORSE);
            this.horse.setTamed(true);
            
            // Dynamic Mount Speed
            double mountSpeed = getConfigDouble("abilities.mount_speed", 0.375);
            this.horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(mountSpeed);
            
            this.horse.addPassenger(entity);
        }
    }

    @Override
    public void tick() {}

    @Override
    public void cleanUp() {
        if (horse != null && horse.isValid()) {
            horse.setTamed(false);
            horse.remove(); 
        }
    }
}