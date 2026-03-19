package com.kairos.handlers.protection;

import org.bukkit.Location;

public interface ProtectionHook {
    String getPluginName();
    
    /** Returns true if the location is inside a claimed/protected region for this specific plugin */
    boolean isClaimed(Location location);
}
