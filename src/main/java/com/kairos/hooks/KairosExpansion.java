package com.kairos.hooks;

import com.kairos.KairosPlugin;
import com.kairos.core.TemperatureManager;
import com.kairos.core.ThirstManager;
import com.kairos.events.base.ActiveDisaster;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class KairosExpansion extends PlaceholderExpansion {

    private final KairosPlugin plugin;
    private final TemperatureManager tempManager;
    private final ThirstManager thirstManager;

    public KairosExpansion(KairosPlugin plugin, TemperatureManager tempManager, ThirstManager thirstManager) {
        this.plugin = plugin;
        this.tempManager = tempManager;
        this.thirstManager = thirstManager;
    }

    @Override
    public String getIdentifier() { return "kairos"; }

    @Override
    public String getAuthor() { return "YourName"; }

    @Override
    public String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public boolean persist() { return true; } 

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) return "";
        Player player = offlinePlayer.getPlayer();

        switch (params.toLowerCase()) {

            case "temperature":
                if (tempManager == null || !tempManager.isEnabled()) return "N/A";
                return String.valueOf((int) tempManager.getTemperature(player));

            case "thirst":
                if (thirstManager == null || !thirstManager.isEnabled()) return "N/A";
                return String.valueOf((int) thirstManager.getThirst(player));

            case "season":
                return plugin.getSeasonManager().getCurrentSeason(player.getWorld()).getName();

            case "season_colored":
                return plugin.getSeasonManager().getCurrentSeason(player.getWorld()).getDisplayName();

            // --- NEW PLACEHOLDERS ---
            case "season_days_passed":
                return String.valueOf(
                        plugin.getSeasonManager().getDaysPassed(player.getWorld())
                );

            case "season_days_remaining":
                int passed = plugin.getSeasonManager().getDaysPassed(player.getWorld());
                int total = plugin.getSeasonManager().getTargetDuration(player.getWorld());
                return String.valueOf(total - passed);
            // ------------------------

            case "active_disasters_count":
                int count = 0;
                for (ActiveDisaster d : ActiveDisaster.ongoingDisasters) {
                    if (d.getWorld().equals(player.getWorld())) count++;
                }
                return String.valueOf(count);

            case "disease_count":
                return String.valueOf(
                        plugin.getDiseaseManager().getActiveDiseases(player).size()
                );
        }

        return null;
    }
}