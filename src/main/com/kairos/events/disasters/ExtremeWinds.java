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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExtremeWinds extends ExtremeWindsTask { 
    
    private double tempForce, breakForce;
    private Particle particle;
    private int maxParticles, windHeight;
    private boolean pushEntities;
    private RepeatingTask windTask; 
    
    private int gustTicks = 0; 
    
    private final List<Entity> affectedEntities = new ArrayList<>();

    public ExtremeWinds(int level, World world) {
        super(DisasterType.EXTREME_WINDS, level, world);
        loadConfig();
    }

    private void loadConfig() {
        this.tempForce = getDoubleOrDefault("extremewinds.force.level_" + level, 1.5);
        this.time = getIntOrDefault("extremewinds.time.level_" + level, 200) * 20;
        this.delay = getIntOrDefault("extremewinds.start_delay", 40) * 20;
        this.maxParticles = getIntOrDefault("extremewinds.max_particles", 100) / 6 * level;
        this.breakForce = getDoubleOrDefault("extremewinds.block_break_force", 1.0);
        this.volume = getDoubleOrDefault("extremewinds.volume", 1.0);
        this.windHeight = getIntOrDefault("extremewinds.wind_height", 64);
        this.pushEntities = getBooleanOrDefault("extremewinds.push_entities", true);
        
        try {
            this.particle = Particle.valueOf(configFile.getString("extremewinds.particle", "CLOUD").toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            this.particle = Particle.CLOUD;
        }
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
                MessageUtils.send(player, "messages.weather.winds.level_" + level, "&cExtreme Winds Incoming!");
            }
        }

        startWindsTask();
    }

    private void startWindsTask() {
        Random rand = new Random();
        
        this.windTask = new RepeatingTask(plugin, delay, 1) { 
            @Override
            public void run() {
                if (time <= 0) {
                    triggerEndCommands(); // NEW!
                    ongoingDisasters.remove(ExtremeWinds.this);
                    affectedEntities.clear();
                    cancel();
                    return;
                }
                time--;

                if (gustTicks > 0) {
                    gustTicks--; 
                } else if (rand.nextDouble() < 0.05) { 
                    gustTicks = rand.nextInt(30) + 10; 
                }

                double currentMultiplier = (gustTicks > 0) ? 0.15 : 0.015;
                double tickForce = tempForce * currentMultiplier;

                Vector speed = new Vector(rand.nextDouble() - 0.5, 0, rand.nextDouble() - 0.5)
                        .normalize().setY(0.15).multiply(tickForce); 

                refreshAffectedEntities();
                
                if (pushEntities) handleEntities(speed);
                handleParticles(rand, speed);
                handleSounds(speed);
            }
        };
    }

    private void refreshAffectedEntities() {
        affectedEntities.clear();
        for (Entity e : world.getEntities()) {
            if (e instanceof LivingEntity && !isEntityTypeProtected(e)) {
                if (e.getLocation().getBlockY() >= windHeight - 7) { 
                    affectedEntities.add(e);
                }
            }
        }
    }

    private void handleEntities(Vector speed) {
        Vector entityWindSpeed = speed.clone().multiply(1.5); 
        
        for (Entity e : affectedEntities) {
            if (e instanceof Player && ((Player) e).isFlying()) continue;

            if (e instanceof LivingEntity) {
                Vector currentVel = e.getVelocity();
                double newY = Math.min(currentVel.getY() + speed.getY(), 0.5); 
                e.setVelocity(currentVel.setY(newY).add(new Vector(speed.getX(), 0, speed.getZ())));
            } else {
                e.setVelocity(entityWindSpeed); 
            }
        }
    }

    private void handleParticles(Random rand, Vector speed) {
        for (Player p : world.getPlayers()) {
            if (p.getLocation().getBlockY() < windHeight) continue;
            
            Location loc = p.getLocation();
            for (int i = 0; i < maxParticles; i++) {
                double offsetX = (rand.nextDouble() - 0.5) * 20;
                double offsetY = (rand.nextDouble() * 10);
                double offsetZ = (rand.nextDouble() - 0.5) * 20;
                p.spawnParticle(particle, loc.clone().add(offsetX, offsetY, offsetZ), 0, speed.getX() * 10, 0.001, speed.getZ() * 10, tempForce);
            }
        }
    }

    private void handleSounds(Vector speed) {
        int soundModulo = (gustTicks > 0) ? 2 : 10;
        if (time % soundModulo != 0) return; 
        
        for (Player p : world.getPlayers()) {
            if (p.getLocation().getBlockY() < windHeight) continue;
            float pitch = (gustTicks > 0) ? 0.8F : 0.5F;
            p.playSound(p.getLocation(), Sound.WEATHER_RAIN, (float) (tempForce * volume), pitch);
        }
    }

    @Override
    public void clear() {
        this.time = 0; 
        triggerEndCommands(); // NEW!
        if (this.windTask != null) {
            this.windTask.cancel(); 
        }
    }
}

abstract class ExtremeWindsTask extends WeatherDisaster {
    public ExtremeWindsTask(DisasterType type, int level, World world) {
        super(type, level, world);
    }
}