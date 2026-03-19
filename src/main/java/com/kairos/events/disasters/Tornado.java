package com.kairos.events.disasters;

import com.kairos.api.DisasterType;
import com.kairos.api.events.DisasterPreStartEvent;
import com.kairos.events.base.ActiveDisaster;
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

public class Tornado extends ActiveDisaster {

    private RepeatingTask task;
    private int time;
    private double size;
    private double pullForce;
    private Location loc;

    public Tornado(int level, World world) {
        super(DisasterType.TORNADO, level, world);
        this.time = getIntOrDefault("tornado.time.level_" + level, 30) * 20;
        this.size = getDoubleOrDefault("tornado.size.level_" + level, 15.0) + (level * 5);
        this.pullForce = getDoubleOrDefault("tornado.pull_force.level_" + level, 0.05) + (level * 0.02);
    }

    @Override
    public void start(World world, Player p, boolean broadcastAllowed) {
        DisasterPreStartEvent event = new DisasterPreStartEvent(this, world, level, p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        Random rand = new Random();
        this.loc = p.getLocation().clone().add((rand.nextDouble() - 0.5) * 40, 0, (rand.nextDouble() - 0.5) * 40);
        this.loc.setY(world.getHighestBlockYAt(this.loc));

        ongoingDisasters.add(this);
        triggerStartCommands(p); // NEW!

        if (broadcastAllowed && isWarningEnabled() && (boolean) worldProfile.getSettings().getOrDefault("event_broadcast", true)) {
            for (Player player : world.getPlayers()) {
                MessageUtils.send(player, "messages.weather.tornado.level_" + level, "&8&lA massive Tornado has touched down nearby!");
            }
        }
        
        startTask();
    }

    private void startTask() {
        Random rand = new Random();
        Vector moveDirection = new Vector(rand.nextDouble() - 0.5, 0, rand.nextDouble() - 0.5).normalize().multiply(0.15 + (level * 0.05));
        
        this.task = new RepeatingTask(plugin, 0, 1) {
            @Override
            public void run() {
                if (time <= 0) {
                    triggerEndCommands(); // NEW!
                    ongoingDisasters.remove(Tornado.this);
                    cancel();
                    return;
                }
                time--;

                loc.add(moveDirection);
                int highest = world.getHighestBlockYAt(loc);
                if (Math.abs(loc.getY() - highest) > 2) {
                    loc.setY(highest);
                }

                double height = size + (level * 5);
                for (int y = 0; y < height; y += 2) {
                    double radius = (y / height) * (size / 2) + 1.0;
                    int particles = (int) (radius * 4);
                    for (int i = 0; i < particles; i++) {
                        double angle = rand.nextDouble() * 2 * Math.PI;
                        double pX = loc.getX() + Math.cos(angle) * radius;
                        double pZ = loc.getZ() + Math.sin(angle) * radius;
                        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pX, loc.getY() + y, pZ, 1, 0, 0, 0, 0.05);
                    }
                }

                if (time % 20 == 0) {
                    world.playSound(loc, Sound.ITEM_ELYTRA_FLYING, 3.0f, 0.5f);
                }

                for (Entity e : world.getNearbyEntities(loc, size, height, size)) {
                    if (isEntityTypeProtected(e)) continue;
                    if (e instanceof Player player && player.isFlying()) continue;

                    Location eLoc = e.getLocation();
                    double diffY = eLoc.getY() - loc.getY();
                    if (diffY < 0 || diffY > height) continue;

                    double distance = eLoc.distance(loc.clone().add(0, diffY, 0));
                    double currentRadius = (diffY / height) * (size / 2) + 1.0;

                    if (distance <= currentRadius + 5.0) {
                        Vector pull = loc.clone().add(0, diffY, 0).toVector().subtract(eLoc.toVector()).normalize().multiply(pullForce);
                        Vector spin = new Vector(-pull.getZ(), 0, pull.getX()).normalize().multiply(pullForce * 2);
                        Vector finalVel = e.getVelocity().add(pull).add(spin);
                        if (finalVel.getY() < 0.5) finalVel.setY(finalVel.getY() + 0.1);
                        e.setVelocity(finalVel);
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