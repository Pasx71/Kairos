package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.Season;
import com.kairos.utils.PlaceholderUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TemperatureManager implements Runnable, Listener {

    private final KairosPlugin plugin;
    private final SeasonManager seasonManager;
    private final FileConfiguration config;

    private final Map<UUID, Double> playerTemperatures = new HashMap<>();
    private final boolean isEnabled;

    public TemperatureManager(KairosPlugin plugin, SeasonManager seasonManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.seasonManager = seasonManager;
        this.config = configManager.getConfig("temperature.yml");
        
        this.isEnabled = config.getBoolean("enabled", true);

        if (isEnabled) {
            Bukkit.getScheduler().runTaskTimer(plugin, this, 40L, 40L); // Runs every 2 seconds
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    public boolean isEnabled() { return isEnabled; }

    public double getTemperature(Player player) {
        return playerTemperatures.getOrDefault(player.getUniqueId(), config.getDouble("settings.base_temperature", 50.0));
    }

@Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            double currentTemp = getTemperature(player);
            double targetTemp = calculateTargetTemperature(player);
            double transitionSpeed = config.getDouble("settings.transition_speed", 2.0);

            double newTemp = currentTemp;
            if (currentTemp < targetTemp) newTemp = Math.min(targetTemp, currentTemp + transitionSpeed);
            else if (currentTemp > targetTemp) newTemp = Math.max(targetTemp, currentTemp - transitionSpeed);

            // API INTEGRATION: Only fire if the temperature actually shifted
            if (newTemp != currentTemp) {
                com.kairos.api.events.PlayerTemperatureChangeEvent event = 
                        new com.kairos.api.events.PlayerTemperatureChangeEvent(player, currentTemp, newTemp);
                Bukkit.getPluginManager().callEvent(event);
                
                if (event.isCancelled()) continue;
                newTemp = event.getNewTemperature(); // Apply modified temp if another plugin changed it
            }

            playerTemperatures.put(player.getUniqueId(), newTemp);
            applyTemperatureEffects(player, newTemp);
        }
    }

    private double calculateTargetTemperature(Player player) {
        double target = config.getDouble("settings.base_temperature", 50.0);

        Biome biome = player.getLocation().getBlock().getBiome();
        
        if (plugin.getBiomeGroupManager().isInGroup(biome, "hot")) {
            target += config.getDouble("modifiers.biomes.hot", 25.0);
        } else if (plugin.getBiomeGroupManager().isInGroup(biome, "cold")) {
            target -= config.getDouble("modifiers.biomes.cold", 25.0);
        }

        Season season = seasonManager.getCurrentSeason(player.getWorld());
        if (season == Season.WINTER) target -= config.getDouble("modifiers.seasons.winter", 20.0);
        else if (season == Season.SUMMER) target += config.getDouble("modifiers.seasons.summer", 20.0);

        long time = player.getWorld().getTime();
        if (time > 13000 && time < 23000) target -= config.getDouble("modifiers.environment.night", 10.0);
        
        if (player.getWorld().hasStorm() && player.getWorld().getHighestBlockYAt(player.getLocation()) <= player.getLocation().getBlockY()) {
            target -= config.getDouble("modifiers.environment.rain", 15.0);
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType().name().contains("LEATHER")) {
                target += config.getDouble("modifiers.armor.leather_piece", 5.0);
            }
        }

        target += getNearbyBlockHeat(player);

        // INTEGRATION: External plugins (Skills, Magic, Custom Races) altering temperature
        String placeholder = config.getString("integration.temperature_modifier.placeholder", "none");
        double scale = config.getDouble("integration.temperature_modifier.scale", 1.0);
        double modifier = PlaceholderUtils.parseDouble(player, placeholder, 0.0);
        target += (modifier * scale);

        return Math.max(0.0, Math.min(100.0, target));
    }

    private double getNearbyBlockHeat(Player player) {
        double heat = 0;
        Location loc = player.getLocation();
        
        List<String> hotBlocks = config.getStringList("modifiers.blocks.hot_materials");
        List<String> coldBlocks = config.getStringList("modifiers.blocks.cold_materials");
        double hotValue = config.getDouble("modifiers.blocks.hot_value", 8.0);
        double coldValue = config.getDouble("modifiers.blocks.cold_value", 3.0);

        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    String matName = loc.clone().add(x, y, z).getBlock().getType().name();
                    if (hotBlocks.contains(matName)) heat += hotValue;
                    else if (coldBlocks.contains(matName)) heat -= coldValue;
                }
            }
        }
        return Math.max(-30.0, Math.min(40.0, heat)); 
    }

    private void applyTemperatureEffects(Player player, double temp) {
        double freezeThreshold = config.getDouble("effects.freezing.threshold", 10.0);
        double heatThreshold = config.getDouble("effects.overheating.threshold", 90.0);

        if (temp <= freezeThreshold) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1, false, false, false));
            player.setFreezeTicks(Math.min(player.getMaxFreezeTicks(), player.getFreezeTicks() + 40));
            if (player.getFreezeTicks() >= player.getMaxFreezeTicks()) {
                player.damage(config.getDouble("effects.freezing.damage", 1.0));
            }
        } else if (temp >= heatThreshold) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 60, 1, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0, false, false, false));
            if (Math.random() < config.getDouble("effects.overheating.damage_chance", 0.2)) {
                player.damage(config.getDouble("effects.overheating.damage", 1.0));
            }
        } else {
            player.setFreezeTicks(Math.max(0, player.getFreezeTicks() - 40));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerTemperatures.remove(e.getPlayer().getUniqueId());
    }
}