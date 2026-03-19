package com.kairos.handlers.protection;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class LandsHook implements ProtectionHook {
    
    private final LandsIntegration landsIntegration;

    public LandsHook(Plugin plugin) {
        this.landsIntegration = LandsIntegration.of(plugin);
    }

    @Override
    public String getPluginName() { return "Lands"; }

    @Override
    public boolean isClaimed(Location location) {
        // If an Area is returned, the land is claimed!
        Area area = landsIntegration.getArea(location);
        return area != null;
    }
}
