package com.kairos.entities.mobs;

import com.kairos.api.entities.CustomEntity;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.utils.HeadUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.inventory.ItemStack;

public class ZombieKnight extends CustomEntity {

    public static final CustomEntityType TYPE = new CustomEntityType(
            "zombie_knight", "Zombie Knight", "&8&l", 30.0, EntityType.ZOMBIE) {
        @Override
        protected CustomEntity instantiateWrapper(LivingEntity entity) {
            return new ZombieKnight(entity);
        }
    };

    private ZombieHorse horse;

    public ZombieKnight(LivingEntity entity) {
        super(entity, TYPE);
        
        entity.getEquipment().setHelmet(HeadUtils.ZOMBIE_KNIGHT.getHead());
        entity.getEquipment().setHelmetDropChance(0f);
        entity.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
        entity.getEquipment().setItemInOffHand(new ItemStack(Material.SHIELD));

        if (!entity.isInsideVehicle()) {
            this.horse = (ZombieHorse) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ZOMBIE_HORSE);
            this.horse.setTamed(true);
            
            // Dynamic Mount Speed
            double mountSpeed = getConfigDouble("abilities.mount_speed", 0.425);
            this.horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(mountSpeed);
            
            this.horse.addPassenger(entity);
        }
    }

    @Override
    public void tick() {}

    @Override
    public void cleanUp() {
        if (horse != null && horse.isValid()) {
            horse.remove(); 
        }
    }
}