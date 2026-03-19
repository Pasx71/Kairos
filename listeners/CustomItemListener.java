package com.kairos.listeners;

import com.kairos.KairosPlugin;
import com.kairos.api.items.CustomItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CustomItemListener implements Listener {

    private final KairosPlugin plugin;

    public CustomItemListener(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        CustomItem customItem = plugin.getItemRegistry().getCustomItem(item);
        
        if (customItem != null) {
            customItem.onInteract(e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player attacker) {
            ItemStack item = attacker.getInventory().getItemInMainHand();
            CustomItem customItem = plugin.getItemRegistry().getCustomItem(item);
            
            if (customItem != null) {
                customItem.onAttack(e, attacker);
            }
        }
    }
}