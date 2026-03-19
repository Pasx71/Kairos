package com.kairos.listeners;

import com.kairos.KairosPlugin;
import com.kairos.api.events.armor.ArmorEquipEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ArmorListener implements Listener {

    private final KairosPlugin plugin;

    public ArmorListener(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    private void checkArmorChange(Player player, ItemStack[] oldArmor) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ItemStack[] newArmor = player.getEquipment().getArmorContents(); // 0: Boots, 1: Legs, 2: Chest, 3: Helmet
            for (int i = 0; i < 4; i++) {
                ItemStack oldItem = oldArmor[i];
                ItemStack newItem = newArmor[i];

                if (oldItem == null && newItem != null) {
                    callEvent(player, i, newItem, true);
                } else if (oldItem != null && newItem == null) {
                    callEvent(player, i, oldItem, false);
                } else if (oldItem != null && !oldItem.equals(newItem)) {
                    callEvent(player, i, oldItem, false);
                    callEvent(player, i, newItem, true);
                }
            }
        }, 1L);
    }

    private void callEvent(Player player, int slotIndex, ItemStack item, boolean isEquip) {
        ArmorEquipEvent.ArmorSlot slot = switch (slotIndex) {
            case 0 -> ArmorEquipEvent.ArmorSlot.FEET;
            case 1 -> ArmorEquipEvent.ArmorSlot.LEGS;
            case 2 -> ArmorEquipEvent.ArmorSlot.CHEST;
            default -> ArmorEquipEvent.ArmorSlot.HEAD;
        };
        Bukkit.getPluginManager().callEvent(new ArmorEquipEvent(player, slot, item, isEquip));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player player) {
            checkArmorChange(player, player.getEquipment().getArmorContents());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player player) {
            checkArmorChange(player, player.getEquipment().getArmorContents());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        checkArmorChange(e.getPlayer(), e.getPlayer().getEquipment().getArmorContents());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDispense(BlockDispenseArmorEvent e) {
        if (e.getTargetEntity() instanceof Player player) {
            checkArmorChange(player, player.getEquipment().getArmorContents());
        }
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {
        String action = e.isEquip() ? "EQUIPPED" : "UNEQUIPPED";
        String itemName = (e.getItem() != null) ? e.getItem().getType().name() : "Nothing";
        
        plugin.getLogger().info("[Armor Event] " + e.getPlayer().getName() + " " + action + " " + itemName + " in slot " + e.getSlot().name());
    }
}