package com.kairos.listeners;

import com.kairos.gui.EventMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        InventoryHolder holder = e.getClickedInventory().getHolder();

        // Check if the inventory belongs to our EventMenu
        if (holder instanceof EventMenu menu) {
            e.setCancelled(true); // Prevent them from taking items!
            
            if (e.getWhoClicked() instanceof Player player) {
                menu.handleClick(player, e.getSlot(), e.getCurrentItem());
            }
        }
    }
}