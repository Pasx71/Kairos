package com.kairos.listeners;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.Season;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Random;

public class WildlifeSpawnListener implements Listener {

    private final KairosPlugin plugin;
    private final Random random = new Random();

    public WildlifeSpawnListener(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAnimalSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL && 
            event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CHUNK_GEN) {
            return;
        }

        if (event.getEntity() instanceof Animals) {
            Season season = plugin.getSeasonManager().getCurrentSeason(event.getLocation().getWorld());
            FileConfiguration config = plugin.getConfigManager().getConfig("seasons/" + season.name().toLowerCase() + ".yml");
            
            double penalty = config.getDouble("spawning.wildlife_spawn_penalty", 0.0);

            if (penalty > 0 && random.nextDouble() < penalty) {
                event.setCancelled(true);
            }
        }
    }
}