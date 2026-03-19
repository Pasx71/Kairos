package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.api.events.ScheduledEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ScheduledEventTask implements Runnable {

    private final ScheduledEventManager manager;

    public ScheduledEventTask(KairosPlugin plugin) {
        this.manager = plugin.getScheduledEventManager();
    }

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            // getFullTime() gives total ticks since world creation. 24000 ticks = 1 day.
            long fullTime = world.getFullTime();
            long currentDay = fullTime / 24000;
            long currentTime = world.getTime(); // Returns 0 - 24000

            for (ScheduledEvent event : manager.getRegisteredEvents()) {
                boolean isActive = manager.isActive(world, event);
                
                long startTime = event.getStartTime();
                long endTime = event.getEndTime();
                
                // Handle wrap-around (e.g. event starts at 23000 and ends at 2000 the next morning)
                boolean isTimeInWindow;
                if (startTime < endTime) {
                    isTimeInWindow = currentTime >= startTime && currentTime < endTime;
                } else {
                    isTimeInWindow = currentTime >= startTime || currentTime < endTime;
                }

                if (!isActive && isTimeInWindow) {
                    // It's the right time! Check if it's the correct interval day
                    if (currentDay % event.getIntervalDays() == 0) {
                        
                        // Check if we haven't already fired it today
                        if (manager.getLastFiredDay(world, event) < currentDay) {
                            manager.setLastFiredDay(world, event, currentDay);
                            manager.setActive(world, event, true);
                            event.start(world);
                        }
                    }
                } else if (isActive && !isTimeInWindow) {
                    // Time window closed. Stop the event.
                    manager.setActive(world, event, false);
                    event.stop(world);
                }
            }
        }
    }
}