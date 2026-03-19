package com.kairos.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import com.kairos.KairosPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageUtils {

    public static void send(CommandSender sender, String path, String defaultMessage, String... placeholders) {
        KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
        
        FileConfiguration config = plugin.getConfigManager().getConfig("messages.yml");
        String message = config.getString(path, defaultMessage);
        
        if (message == null || message.trim().isEmpty() || message.equalsIgnoreCase("none")) {
            return; 
        }
        
        if (placeholders != null && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        
        sender.sendMessage(ColorUtils.colorize(message));
    }

    public static void broadcast(String path, String defaultMessage, String... placeholders) {
        KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
        
        FileConfiguration config = plugin.getConfigManager().getConfig("messages.yml");
        String message = config.getString(path, defaultMessage);
        
        if (message == null || message.trim().isEmpty() || message.equalsIgnoreCase("none")) {
            return; 
        }
        
        if (placeholders != null && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        
        plugin.getServer().broadcastMessage(ColorUtils.colorize(message));
    }
}
