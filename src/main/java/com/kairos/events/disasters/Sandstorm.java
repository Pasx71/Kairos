package com.kairos.events.disasters;

import com.kairos.api.DisasterType;
import com.kairos.api.events.DisasterPreStartEvent;
import com.kairos.events.base.WeatherDisaster;
import com.kairos.utils.MessageUtils;
import com.kairos.utils.RepeatingTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class Sandstorm extends WeatherDisaster {

    private RepeatingTask task;
    private final BlockData sandData;
    private final BlockData redSandData;

    public Sandstorm(int level, World world) {
        super(DisasterType.SANDSTORM, level, world);
        this.time = getIntOrDefault("sandstorm.time.level_" + level, 200) * 20;
        this.delay = getIntOrDefault("sandstorm.start_delay", 40) * 20;
        
        this.sandData = Material.SAND.createBlockData();
        this.redSandData = Material.RED_SAND.createBlockData();
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
                MessageUtils.send(player, "messages.weather.sandstorm.level_" + level, "&eA choking Sandstorm approaches!");
            }
        }
        startTask();
    }

    private void startTask() {
        Random rand = new Random();
        
        this.task = new RepeatingTask(plugin, delay, 5) {
            @Override
            public void run() {
                if (time <= 0) {
                    triggerEndCommands(); // NEW!
                    ongoingDisasters.remove(Sandstorm.this);
                    cancel();
                    return;
                }
                time -= 5;

                for (Player p : world.getPlayers()) {
                    Biome biome = p.getLocation().getBlock().getBiome();
                    boolean isDesert = biome.name().contains("DESERT");
                    boolean isBadlands = biome.name().contains("BADLANDS");

                    if (!isDesert && !isBadlands) continue;

                    Location loc = p.getLocation();
                    boolean hasSky = world.getHighestBlockYAt(loc) <= loc.getBlockY();

                    BlockData dustType = isBadlands ? redSandData : sandData;
                    
                    p.spawnParticle(Particle.FALLING_DUST, loc.clone().add(0, 5, 0), 150 * level, 15, 10, 15, dustType);
                    
                    if (time % 20 == 0) {
                        p.playSound(loc, Sound.AMBIENT_BASALT_DELTAS_ADDITIONS, 1.0f, 0.8f);
                    }

                    if (hasSky) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true, false, false));
                        if (level >= 3 && rand.nextDouble() < 0.05) {
                            p.damage(1.0); 
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
