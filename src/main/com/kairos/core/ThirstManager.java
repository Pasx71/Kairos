package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.utils.PlaceholderUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ThirstManager implements Runnable, Listener {

    private final KairosPlugin plugin;
    private final TemperatureManager tempManager;
    private final FileConfiguration config;

    private final Map<UUID, Double> playerThirst = new HashMap<>();
    private final boolean isEnabled;

    public ThirstManager(KairosPlugin plugin, TemperatureManager tempManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.tempManager = tempManager;
        this.config = configManager.getConfig("thirst.yml");

        this.isEnabled = config.getBoolean("enabled", true);

        if (isEnabled) {
            Bukkit.getScheduler().runTaskTimer(plugin, this, 60L, 60L); // Runs every 3 seconds
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    public boolean isEnabled() { return isEnabled; }

    public double getThirst(Player player) {
        return playerThirst.getOrDefault(player.getUniqueId(), 100.0);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            double currentThirst = getThirst(player);
            
            double dehydrationRate = config.getDouble("rates.base_drain", 0.5);

            if (tempManager != null && tempManager.getTemperature(player) > config.getDouble("rates.heat_threshold", 75.0)) {
                dehydrationRate += config.getDouble("rates.heat_drain_penalty", 1.0); 
            }

            if (player.isSprinting()) {
                dehydrationRate += config.getDouble("rates.sprint_drain_penalty", 0.5);
            }

            // INTEGRATION: External plugins (Skills, Races) altering dehydration rate
            String placeholder = config.getString("integration.thirst_resistance.placeholder", "none");
            double scale = config.getDouble("integration.thirst_resistance.scale", 0.0);
            double resistance = PlaceholderUtils.parseDouble(player, placeholder, 0.0);
            
            dehydrationRate = Math.max(0.0, dehydrationRate - (resistance * scale));

            currentThirst = Math.max(0.0, currentThirst - dehydrationRate);
            playerThirst.put(player.getUniqueId(), currentThirst);

            applyThirstEffects(player, currentThirst);
        }
    }

    private void applyThirstEffects(Player player, double thirst) {
        if (thirst <= 0.0) {
            player.damage(config.getDouble("effects.dehydrated.damage", 1.0)); 
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false, false));
        } else if (thirst <= config.getDouble("effects.thirsty.threshold", 20.0)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 0, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 80, 0, false, false, false));
        }
    }

    // INTEGRATION FIX: Ignore if another plugin cancels the consumption
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material item = event.getItem().getType();
        double currentThirst = getThirst(player);
        double restored = 0;

        if (item == Material.POTION) {
            PotionMeta meta = (PotionMeta) event.getItem().getItemMeta();
            if (meta != null && meta.getBasePotionData().getType() == PotionType.WATER) {
                restored = config.getDouble("hydration_items.WATER_BOTTLE", 40.0);
            }
        } 
        else {
            restored = config.getDouble("hydration_items." + item.name(), 0.0);
        }

        if (restored > 0) {
            playerThirst.put(player.getUniqueId(), Math.min(100.0, currentThirst + restored));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerThirst.remove(e.getPlayer().getUniqueId());
    }
}
