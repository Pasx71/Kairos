package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.gui.EventMenu;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DisasterEventHandler {
    
    private boolean isEnabled = true; // Changed to private!

    // Add this getter!
    public boolean isEnabled() {
        return isEnabled;
    }

    public void openGUI(Player player) {
        KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
        EventMenu menu = new EventMenu(plugin);
        player.openInventory(menu.getInventory());
    }
}