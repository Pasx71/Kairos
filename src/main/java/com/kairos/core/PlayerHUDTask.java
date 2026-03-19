package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.utils.ColorUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerHUDTask implements Runnable {

    private final KairosPlugin plugin;
    private final TemperatureManager tempManager;
    private final ThirstManager thirstManager;

    public PlayerHUDTask(KairosPlugin plugin, TemperatureManager tempManager, ThirstManager thirstManager) {
        this.plugin = plugin;
        this.tempManager = tempManager;
        this.thirstManager = thirstManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            StringBuilder hud = new StringBuilder();

            boolean tempEnabled = tempManager != null && tempManager.isEnabled();
            boolean thirstEnabled = thirstManager != null && thirstManager.isEnabled();

            // Format Temperature
            if (tempEnabled) {
                double temp = tempManager.getTemperature(player);
                String color = temp > 80 ? "&c" : (temp < 20 ? "&b" : "&a");
                hud.append(color).append("🌡 ").append((int) temp).append("°");
            }

            // Divider
            if (tempEnabled && thirstEnabled) {
                hud.append(" &8| ");
            }

            // Format Thirst
            if (thirstEnabled) {
                double thirst = thirstManager.getThirst(player);
                String color = thirst < 20 ? "&c" : (thirst < 50 ? "&e" : "&b");
                hud.append(color).append("💧 ").append((int) thirst).append("%");
            }

            // Send to Action Bar
            if (hud.length() > 0) {
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR, 
                        new TextComponent(ColorUtils.colorize(hud.toString()))
                );
            }
        }
    }
}
