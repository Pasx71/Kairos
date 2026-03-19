package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.api.DisasterType;
import com.kairos.handlers.protection.ProtectionType;
import com.kairos.handlers.protection.ProtectionManager;
import com.kairos.utils.PlaceholderUtils;
import com.kairos.world.Difficulty;
import com.kairos.world.WorldManager;
import com.kairos.world.WorldProfile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class DisasterScheduler implements Runnable {

    private final KairosPlugin plugin;
    private final WorldManager worldManager;
    private final DisasterRegistry registry;
    private final PlayerDataManager dataManager;
    private final ProtectionManager protectionManager;
    private final Random random = new Random();

    public DisasterScheduler(KairosPlugin plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
        this.registry = plugin.getDisasterRegistry();
        this.dataManager = plugin.getPlayerDataManager();
        this.protectionManager = plugin.getProtectionManager();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            
            WorldProfile profile = worldManager.getProfile(player.getWorld());
            if (profile == null || profile.isPlayerWhitelisted(player.getUniqueId())) continue;

            // Updated to pass the player into generateTimer for PAPI integration
            int currentTimer = dataManager.getTimer(player.getUniqueId(), generateTimer(profile.getDifficulty(), player));
            if (currentTimer > 0) {
                dataManager.setTimer(player.getUniqueId(), currentTimer - 1);
                continue;
            }

            if (protectionManager.isProtected(player.getLocation(), ProtectionType.DISASTER_SPAWN)) {
                dataManager.setTimer(player.getUniqueId(), 200); 
                continue;
            }

            List<DisasterType> validDisasters = new ArrayList<>();
            for (DisasterType disaster : registry.getAllRegisteredDisasters()) {
                if (!profile.canSpawn(disaster)) continue;
                if (registry.checkConditions(disaster, player)) {
                    validDisasters.add(disaster);
                }
            }

            if (!validDisasters.isEmpty()) {
                double totalWeight = 0.0;
                for (DisasterType dt : validDisasters) totalWeight += dt.getChance();
                
                double randomVal = random.nextDouble() * totalWeight;
                DisasterType chosenDisaster = null;
                
                for (DisasterType dt : validDisasters) {
                    randomVal -= dt.getChance();
                    if (randomVal <= 0.0) {
                        chosenDisaster = dt;
                        break;
                    }
                }
                
                int level = profile.getDifficulty().simulateLevel(random);
                registry.startDisaster(chosenDisaster, level, player.getWorld(), player, true);
            }

            // Reset the player's timer, parsing PAPI again
            dataManager.setTimer(player.getUniqueId(), generateTimer(profile.getDifficulty(), player));
        }
    }

    private int generateTimer(Difficulty difficulty, Player player) {
        int base = difficulty.getTimerBase();
        int offset = difficulty.getOffset();
        int modifier = offset > 0 ? random.nextInt(offset * 2) - offset : 0;
        int finalTimer = base + modifier;

        // GRACEFUL INTEGRATION: Apply external system modifiers (Karma, etc)
        FileConfiguration config = plugin.getConfigManager().getConfig("config.yml");
        
        if (config.getBoolean("integration.enabled", false)) {
            String placeholder = config.getString("integration.disaster_frequency.placeholder", "none");
            double scale = config.getDouble("integration.disaster_frequency.scale", 0.0);
            
            double pValue = PlaceholderUtils.parseDouble(player, placeholder, 0.0);
            // Example: Bad karma (-50) * scale (2.0) = -100 seconds (Disasters happen faster!)
            finalTimer += (int) (pValue * scale);
        }

        return Math.max(10, finalTimer); // Absolute minimum 10 seconds
    }
}