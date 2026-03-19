package com.kairos.api.events.armor;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ArmorEquipEvent extends Event implements Cancellable {
    
    public enum ArmorSlot { HEAD, CHEST, LEGS, FEET }

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ArmorSlot slot;
    private final ItemStack item;
    private final boolean isEquip;
    private boolean cancelled = false;

    public ArmorEquipEvent(Player player, ArmorSlot slot, ItemStack item, boolean isEquip) {
        this.player = player;
        this.slot = slot;
        this.item = item;
        this.isEquip = isEquip;
    }

    public Player getPlayer() { return player; }
    public ArmorSlot getSlot() { return slot; }
    public ItemStack getItem() { return item; }
    public boolean isEquip() { return isEquip; } // True = Equip, False = Unequip

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}