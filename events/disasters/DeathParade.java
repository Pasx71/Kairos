package com.kairos.events.disasters;

import com.kairos.api.DisasterType;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.events.base.PurgeDisaster;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import java.util.Random;

public class DeathParade extends PurgeDisaster {

    public DeathParade(int level, World world) { super(DisasterType.DEATH_PARADE, level, world); }

    @Override
    protected void spawnDefaultMob(Location loc) {
        Random rand = new Random();
        int r = rand.nextInt(100);
        LivingEntity spawned = null;

        if (r < 20) spawned = (LivingEntity) world.spawnEntity(loc, EntityType.ZOMBIE);
        else if (r < 40) spawned = (LivingEntity) world.spawnEntity(loc, EntityType.SKELETON);
        else if (r < 50) spawned = CustomEntityType.getCustomEntityType("skeleton_knight").spawn(loc).getBukkitEntity();
        else if (r < 60) spawned = CustomEntityType.getCustomEntityType("zombie_knight").spawn(loc).getBukkitEntity();
        else if (r < 70) spawned = CustomEntityType.getCustomEntityType("ghoul").spawn(loc).getBukkitEntity();
        else if (r < 80) spawned = CustomEntityType.getCustomEntityType("psyco").spawn(loc).getBukkitEntity();
        else if (r < 90) spawned = CustomEntityType.getCustomEntityType("scarecrow").spawn(loc).getBukkitEntity();
        else if (r < 95) spawned = CustomEntityType.getCustomEntityType("vampire").spawn(loc).getBukkitEntity();
        else spawned = CustomEntityType.getCustomEntityType("lost_soul").spawn(loc).getBukkitEntity();

        if (spawned != null) activeMobs.add(spawned);
    }
}