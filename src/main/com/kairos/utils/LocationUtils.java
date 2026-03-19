package com.kairos.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.Random;

public class LocationUtils {
    
    private static final Random rand = new Random();

    /** Gets a random surface location within a square radius */
    public static Location getRandomSurfaceLocation(Location center, int radius) {
        int xOffset = rand.nextInt(radius * 2) - radius;
        int zOffset = rand.nextInt(radius * 2) - radius;
        
        Location target = center.clone().add(xOffset, 0, zOffset);
        return getHighestSolidBlock(target).getLocation().add(0.5, 1, 0.5);
    }

    public static Block getHighestSolidBlock(Location loc) {
        int y = loc.getWorld().getMaxHeight();
        while (y > loc.getWorld().getMinHeight()) {
            Block block = loc.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            if (block.getType().isSolid()) {
                return block;
            }
            y--;
        }
        return loc.getBlock(); // Fallback
    }

    /** Simple Raycast checking for solid blocks */
    public static boolean hasLineOfSight(Location initial, Location target) {
        Vector vec = target.toVector().subtract(initial.toVector()).normalize();
        double distance = initial.distance(target);
        
        Location temp = initial.clone();
        for (int i = 0; i < distance; i++) {
            if (temp.add(vec).getBlock().getType().isSolid()) {
                return false;
            }
        }
        return true;
    }
}