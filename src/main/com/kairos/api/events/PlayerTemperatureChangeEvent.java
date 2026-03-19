package com.kairos.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerTemperatureChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final double oldTemperature;
    private double newTemperature;
    private boolean cancelled;

    public PlayerTemperatureChangeEvent(Player player, double oldTemperature, double newTemperature) {
        this.player = player;
        this.oldTemperature = oldTemperature;
        this.newTemperature = newTemperature;
    }

    public Player getPlayer() { return player; }
    public double getOldTemperature() { return oldTemperature; }
    public double getNewTemperature() { return newTemperature; }
    
    // Allows another plugin to forcefully change the outcome temperature
    public void setNewTemperature(double newTemperature) { this.newTemperature = newTemperature; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}