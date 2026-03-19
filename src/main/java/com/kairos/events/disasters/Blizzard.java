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
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Blizzard extends WeatherDisaster {

    private RepeatingTask task;
    private double damage;
    private int freezeHeight;

    public Blizzard(int level, World world) {
        super(DisasterType.BLIZZARD, level, world);
        this.time = getIntOrDefault("blizzard.time.level_" + level, 200) * 20;
        this.delay = getIntOrDefault("blizzard.start_delay", 40) * 20;
        this.damage = getDoubleOrDefault("blizzard.damage", 2.0);
        this.freezeHeight = getIntOrDefault("blizzard.min_freezing_height", 50);
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
                MessageUtils.send(player, "messages.weather.blizzard.level_" + level, "&bA massive Blizzard is rolling in! Seek shelter!");
            }
        }
        
        world.setStorm(true);
        startTask();
    }

    private void startTask() {
        this.task = new RepeatingTask(plugin, delay, 10) { 
            @Override
            public void run() {
                if (time <= 0) {
                    triggerEndCommands(); // NEW!
                    ongoingDisasters.remove(Blizzard.this);
                    cancel();
                    return;
                }
                time -= 10;

                for (Player p : world.getPlayers()) {
                    if (p.getLocation().getBlockY() < freezeHeight) continue;

                    Location loc = p.getLocation();
                    p.spawnParticle(Particle.SNOWFLAKE, loc.clone().add(0, 5, 0), 200 * level, 15, 10, 15, 0.05);
                    p.playSound(loc, Sound.ITEM_ELYTRA_FLYING, 0.1f, 0.5f);

                    byte lightLevel = loc.getBlock().getLightFromBlocks();
                    boolean hasSky = loc.getWorld().getHighestBlockYAt(loc) <= loc.getBlockY();
                    
                    if (hasSky && lightLevel < 10) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, true, false, false));
                        p.setFreezeTicks(Math.min(p.getMaxFreezeTicks(), p.getFreezeTicks() + 40));
                        
                        if (p.getFreezeTicks() >= p.getMaxFreezeTicks()) {
                            p.damage(damage);
                        }
                    } else {
                        p.setFreezeTicks(Math.max(0, p.getFreezeTicks() - 60));
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
