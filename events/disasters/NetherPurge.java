package com.kairos.events.disasters;

import com.kairos.api.DisasterType;
import com.kairos.events.base.PurgeDisaster;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import java.util.Random;

public class NetherPurge extends PurgeDisaster {

    public NetherPurge(int level, World world) { super(DisasterType.NETHER_PURGE, level, world); }

    @Override
    protected void spawnDefaultMob(Location loc) {
        Random rand = new Random();
        int r = rand.nextInt(100);
        LivingEntity spawned;

        if (r < 30) spawned = (LivingEntity) world.spawnEntity(loc, EntityType.WITHER_SKELETON);
        else if (r < 55) spawned = (LivingEntity) world.spawnEntity(loc, EntityType.MAGMA_CUBE);
        else if (r < 80) spawned = (LivingEntity) world.spawnEntity(loc, EntityType.ZOMBIFIED_PIGLIN);
        else spawned = (LivingEntity) world.spawnEntity(loc, EntityType.ZOGLIN);
        
        if (spawned != null) {
            onMobSpawned(spawned);
            activeMobs.add(spawned);
        }
    }

    @Override
    protected void onMobSpawned(LivingEntity entity) {
        // Tag Wither Skeletons so we can control their skull drop rates!
        if (entity.getType() == EntityType.WITHER_SKELETON) {
            entity.getPersistentDataContainer().set(new NamespacedKey(plugin, "kairos_purge_mob"), PersistentDataType.BYTE, (byte) 1);
        }
    }
}