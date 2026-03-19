package com.kairos.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class RepeatingTask extends BukkitRunnable {
    public RepeatingTask(Plugin plugin, int delayTicks, int periodTicks) {
        this.runTaskTimer(plugin, delayTicks, periodTicks);
    }
}