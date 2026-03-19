package com.kairos.events.disasters;

import com.kairos.api.DisasterType;
import com.kairos.api.events.DisasterPreStartEvent;
import com.kairos.events.base.ActiveDisaster;
import com.kairos.utils.MessageUtils;
import com.kairos.utils.RepeatingTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class Earthquake extends ActiveDisaster {

    private RepeatingTask task;
    private int time;
    private final double force;
    private final int radius;
    private Location epicenter;

    public Earthquake(int level, World world) {
        super(DisasterType.EARTHQUAKE, level, world);
        this.time = getIntOrDefault("earthquake.time.level_" + level, 15) * 20; 
        this.force = getDoubleOrDefault("earthquake.force.level_" + level, 0.4) * level;
        this.radius = getIntOrDefault("earthquake.radius.level_" + level, 40) * level;
    }

    @Override
    public void start(World world, Player p, boolean broadcastAllowed) {
        DisasterPreStartEvent event = new DisasterPreStartEvent(this, world, level, p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        this.epicenter = p.getLocation();
        ongoingDisasters.add(this);
        
        // NEW: Trigger start commands!
        triggerStartCommands(p);

        if (broadcastAllowed && isWarningEnabled() && (boolean) worldProfile.getSettings().getOrDefault("event_broadcast", true)) {
            for (Player player : world.getPlayers()) {
                if (player.getLocation().distance(epicenter) <= radius * 2) {
                    MessageUtils.send(player, "messages.weather.earthquake.level_" + level, "&4&lThe ground begins to violently shake!");
                }
            }
        }
        
        startTask();
    }

    private void startTask() {
        Random rand = new Random();
        this.task = new RepeatingTask(plugin, 0, 1) { 
            @Override
            public void run() {
                if (time <= 0) {
                    triggerEndCommands(); // NEW: Trigger end commands!
                    ongoingDisasters.remove(Earthquake.this);
                    cancel();
                    return;
                }
                time--;

                for (Entity e : world.getNearbyEntities(epicenter, radius, radius, radius)) {
                    if (isEntityTypeProtected(e)) continue;
                    if (e instanceof Player player && player.isFlying()) continue;

                    if (e.isOnGround()) {
                        double xShift = (rand.nextDouble() - 0.5) * force;
                        double zShift = (rand.nextDouble() - 0.5) * force;
                        double yShift = (rand.nextDouble() * 0.3); 
                        e.setVelocity(e.getVelocity().add(new Vector(xShift, yShift, zShift)));
                    }

                    if (time % 10 == 0 && e instanceof Player player) {
                        player.playSound(player.getLocation(), Sound.ENTITY_MINECART_RIDING, 1.0f, 0.1f);
                        if (rand.nextDouble() < 0.2) {
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.1f);
                        }
                    }
                }
            }
        };
    }

    @Override
    public void clear() {
        this.time = 0;
        triggerEndCommands(); // NEW: Ensure commands fire even if forcefully stopped
        if (this.task != null) this.task.cancel();
    }
}