package com.kairos.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderUtils {

    /**
     * Safely parses a PAPI placeholder into a double. 
     * E.g., turns "%karma_score%" into 50.0.
     */
    public static double parseDouble(Player player, String placeholder, double fallback) {
        if (placeholder == null || placeholder.isEmpty() || placeholder.equalsIgnoreCase("none")) {
            return fallback;
        }
        
        // Check if PlaceholderAPI is actually installed on the server
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return parseWithPAPI(player, placeholder, fallback);
        }
        
        return fallback;
    }

    // Isolated method to prevent NoClassDefFoundError if PAPI is missing
    private static double parseWithPAPI(Player player, String placeholder, double fallback) {
        try {
            String parsed = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, placeholder);
            return Double.parseDouble(parsed);
        } catch (Exception e) {
            // The placeholder didn't return a valid number, or player was null
            return fallback;
        }
    }
}