package com.kairos.events.disasters;

import com.kairos.api.DisasterType;
import com.kairos.api.events.DisasterPreStartEvent;
import com.kairos.events.base.WeatherDisaster;
import com.kairos.utils.MessageUtils;
import com.kairos.utils.RepeatingTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class Hurricane extends WeatherDisaster {

    private RepeatingTask task;
    private double windForce;
    private int lightningFreq;

    public Hurricane(int level, World world) {
        super(DisasterType.HURRICANE, level, world);
        this.time = getIntOrDefault("hurricane.time.level_" + level, 60) * 20;
        this.delay = getIntOrDefault("hurricane.start_delay", 40) * 20;
        this.windForce = getDoubleOrDefault("hurricane.wind_force.level_" + level, 0.2) + (level * 0.05);
        this.lightningFreq = getIntOrDefault("hurricane.lightning_freq.level_" + level, 100) / level;
    }

    @Override
    public void start(World world, Player p, boolean broadcastAllowed) {
        DisasterPreStartEvent event = new DisasterPreStartEvent(this, world, level, p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        updateWeatherSettings();
        ongoingDisasters.add(this);
        triggerStartCommands(p); // NEW!

        if (broadcastAllowed && isWarningEnabled() && (boolean) worldProfile.getSettings().getOrDefault("event_broadcast", true)) {
            for (Player player : world.getPlayers()) {
                MessageUtils.send(player, "messages.weather.hurricane.level_" + level, "&1&lA category " + level + " Hurricane has struck!");
            }
        }
        
        world.setStorm(true);
        world.setThundering(true);
        world.setThunderDuration(time);
        
        startTask();
    }

    private void startTask() {
        Random rand = new Random();
        Vector windDir = new Vector(rand.nextDouble() - 0.5, 0, rand.nextDouble() - 0.5).normalize();
        
        this.task = new RepeatingTask(plugin, delay, 2) { 
            @Override
            public void run() {
                if (time <= 0) {
                    triggerEndCommands(); // NEW!
                    ongoingDisasters.remove(Hurricane.this);
                    world.setThundering(false);
                    world.setStorm(false);
                    cancel();
                    return;
                }
                time -= 2;

                for (Player p : world.getPlayers()) {
                    Location loc = p.getLocation();
                    boolean hasSky = world.getHighestBlockYAt(loc) <= loc.getBlockY();

                    if (hasSky) {
                        p.spawnParticle(Particle.CLOUD, loc.clone().add(0, 5, 0), 30 * level, 10, 5, 10, 0.1);
                        p.spawnParticle(Particle.WATER_SPLASH, loc.clone().add(0, 2, 0), 20 * level, 10, 2, 10, 0.1);

                        if (time % 20 == 0) {
                            p.playSound(loc, Sound.WEATHER_RAIN, 2.0f, 0.5f);
                            p.playSound(loc, Sound.ITEM_ELYTRA_FLYING, 1.0f, 0.5f);
                        }

                        if (rand.nextInt(Math.max(10, lightningFreq)) == 0) {
                            Location strikeLoc = loc.clone().add((rand.nextDouble() - 0.5) * 40, 0, (rand.nextDouble() - 0.5) * 40);
                            strikeLoc.setY(world.getHighestBlockYAt(strikeLoc));
                            world.strikeLightning(strikeLoc);
                        }
                    }

                    for (Entity e : p.getNearbyEntities(20, 20, 20)) {
                        if (isEntityTypeProtected(e)) continue;
                        if (world.getHighestBlockYAt(e.getLocation()) <= e.getLocation().getBlockY() + 1) {
                            if (e instanceof Player target && target.isFlying()) continue;
                            
                            Vector current = e.getVelocity();
                            double gustMultiplier = rand.nextDouble() > 0.8 ? 2.0 : 1.0;
                            Vector appliedWind = windDir.clone().multiply(windForce * gustMultiplier);
                            
                            e.setVelocity(current.setY(Math.min(current.getY(), 0.3)).add(appliedWind));
                        }
                    }
                }
            }
        };
    }

    @Override
    public void clear() {
        this.time = 0;
        triggerEndCommands(); // NEW!
        if (this.task != null) this.task.cancel();
    }
}
