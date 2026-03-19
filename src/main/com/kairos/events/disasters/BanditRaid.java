package com.kairos.events.disasters;

import com.kairos.api.DisasterType;
import com.kairos.events.base.PurgeDisaster;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import java.util.Random;

public class BanditRaid extends PurgeDisaster {

    public BanditRaid(int level, World world) { super(DisasterType.BANDIT_RAID, level, world); }

    @Override
    protected void spawnDefaultMob(Location loc) {
        Random rand = new Random();
        int r = rand.nextInt(100);
        LivingEntity spawned;

        if (r < 50) spawned = (LivingEntity) world.spawnEntity(loc, EntityType.PILLAGER);
        else if (r < 85) spawned = (LivingEntity) world.spawnEntity(loc, EntityType.VINDICATOR);
        else spawned = (LivingEntity) world.spawnEntity(loc, EntityType.RAVAGER);
        
        if (spawned != null) {
            onMobSpawned(spawned);
            activeMobs.add(spawned);
        }
    }

    @Override
    protected void onMobSpawned(LivingEntity entity) {
        // Ensure Ravagers always have a Pillager riding them!
        if (entity.getType() == EntityType.RAVAGER && entity.getPassengers().isEmpty()) {
            entity.addPassenger(world.spawnEntity(entity.getLocation(), EntityType.PILLAGER));
        }
    }
}
