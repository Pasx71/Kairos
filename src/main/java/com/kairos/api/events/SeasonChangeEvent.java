package com.kairos.api.events;

import com.kairos.api.seasons.Season;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SeasonChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final World world;
    private final Season oldSeason;
    private final Season newSeason;

    public SeasonChangeEvent(World world, Season oldSeason, Season newSeason) {
        this.world = world;
        this.oldSeason = oldSeason;
        this.newSeason = newSeason;
    }

    public World getWorld() { return world; }
    public Season getOldSeason() { return oldSeason; }
    public Season getNewSeason() { return newSeason; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}