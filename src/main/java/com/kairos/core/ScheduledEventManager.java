package com.kairos.core;

import com.kairos.api.events.ScheduledEvent;
import org.bukkit.World;

import java.util.*;

public class ScheduledEventManager {

    private final List<ScheduledEvent> registeredEvents = new ArrayList<>();
    
    // Tracks which events are currently running in which worlds
    private final Map<World, Set<ScheduledEvent>> activeEvents = new HashMap<>();
    
    // Tracks the last in-game day an event fired (prevents /time set night abuse)
    private final Map<World, Map<ScheduledEvent, Long>> lastFiredDay = new HashMap<>();

    public void registerEvent(ScheduledEvent event) {
        registeredEvents.add(event);
    }

    public List<ScheduledEvent> getRegisteredEvents() {
        return registeredEvents;
    }

    public boolean isActive(World world, ScheduledEvent event) {
        return activeEvents.getOrDefault(world, Collections.emptySet()).contains(event);
    }

    public void setActive(World world, ScheduledEvent event, boolean active) {
        activeEvents.computeIfAbsent(world, k -> new HashSet<>());
        if (active) {
            activeEvents.get(world).add(event);
        } else {
            activeEvents.get(world).remove(event);
        }
    }

    public long getLastFiredDay(World world, ScheduledEvent event) {
        return lastFiredDay.getOrDefault(world, Collections.emptyMap()).getOrDefault(event, -1L);
    }

    public void setLastFiredDay(World world, ScheduledEvent event, long day) {
        lastFiredDay.computeIfAbsent(world, k -> new HashMap<>()).put(event, day);
    }
    
    public void cleanUpWorld(World world) {
        activeEvents.remove(world);
        lastFiredDay.remove(world);
    }
}