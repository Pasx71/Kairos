package com.kairos.events.disasters;

import com.kairos.api.DisasterType;
import com.kairos.events.base.PurgeDisaster;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import java.util.Random;

public class Infestation extends PurgeDisaster {

    public Infestation(int level, World world) { super(DisasterType.INFESTATION, level, world); }

    @Override
    protected void spawnDefaultMob(Location loc) {
        Random rand = new Random();
        int r = rand.nextInt(100);
        LivingEntity spawned;

        if (r < 40) spawned = (LivingEntity) world.spawnEntity(loc, EntityType.SPIDER);
        else if (r < 75) spawned = (LivingEntity) world.spawnEntity(loc, EntityType.CAVE_SPIDER);
        else spawned = (LivingEntity) world.spawnEntity(loc, EntityType.SILVERFISH);
        
        if (spawned != null) activeMobs.add(spawned);
    }
}
