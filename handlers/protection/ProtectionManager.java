package com.kairos.handlers.protection;

import com.kairos.KairosPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ProtectionManager {
    
    private final KairosPlugin plugin;
    private final List<ProtectionHook> hooks = new ArrayList<>();

    public ProtectionManager(KairosPlugin plugin) {
        this.plugin = plugin;
        registerHooks();
    }

    private void registerHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            hooks.add(new WorldGuardHook());
            Bukkit.getLogger().info("[Kairos] Hooked into WorldGuard for claim protection!");
        }
        
        if (Bukkit.getPluginManager().isPluginEnabled("Lands")) {
            hooks.add(new LandsHook(plugin));
            Bukkit.getLogger().info("[Kairos] Hooked into Lands for claim protection!");
        }
    }

    /**
     * Checks if a location is protected against a SPECIFIC Kairos mechanic (e.g., MOB_SPAWN).
     */
    public boolean isProtected(Location loc, ProtectionType type) {
        FileConfiguration config = plugin.getConfigManager().getConfig("protection.yml");
        
        for (ProtectionHook hook : hooks) {
            if (hook.isClaimed(loc)) {
                
                // Construct the config path dynamically (e.g., "hooks.lands.prevent_mob_spawn")
                String path = "hooks." + hook.getPluginName().toLowerCase() + ".prevent_" + type.name().toLowerCase();
                
                // If the config says this plugin protects against this mechanic, return true!
                if (config.getBoolean(path, true)) {
                    return true;
                }
            }
        }
        return false;
    }
}
