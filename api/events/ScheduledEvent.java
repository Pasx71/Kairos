package com.kairos.api.events;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class ScheduledEvent {
    
    protected final String id;
    protected final String displayName;
    protected final FileConfiguration config;

    public ScheduledEvent(String id, String displayName, FileConfiguration config) {
        this.id = id;
        this.displayName = displayName;
        this.config = config;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }

    /** How often the event happens. e.g., 7 = Every 7 Minecraft days. 1 = Every day. */
    public abstract int getIntervalDays();

    /** The tick time the event starts (e.g., 13000 for Sunset) */
    public abstract long getStartTime();

    /** The tick time the event stops (e.g., 23000 for Dawn) */
    public abstract long getEndTime();

    /** Triggered when the time window opens */
    public abstract void start(World world);

    /** Triggered when the time window closes */
    public abstract void stop(World world);
}