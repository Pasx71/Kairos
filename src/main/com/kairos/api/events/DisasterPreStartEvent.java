package com.kairos.api.events;

import com.kairos.events.base.ActiveDisaster;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DisasterPreStartEvent extends Event implements Cancellable {

    private final ActiveDisaster disaster;
    private final World world;
    private final Player player;
    private final int level;
    private boolean cancelled;

    private static final HandlerList handlers = new HandlerList();

    public DisasterPreStartEvent(ActiveDisaster disaster, World world, int level, Player player) {
        this.disaster = disaster;
        this.world = world;
        this.level = level;
        this.player = player;
    }

    public ActiveDisaster getDisaster() { return disaster; }
    public Player getPlayer() { return player; }
    public World getWorld() { return world; }
    public int getLevel() { return level; }

    @Override 
    public boolean isCancelled() { return cancelled; }
    
    @Override 
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    
    @Override 
    public HandlerList getHandlers() { return handlers; }
    
    public static HandlerList getHandlerList() { return handlers; }
}