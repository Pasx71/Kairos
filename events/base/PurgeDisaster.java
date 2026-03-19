package com.kairos.events.base;

import com.kairos.api.DisasterType;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.utils.ColorUtils;
import com.kairos.utils.LocationUtils;
import com.kairos.utils.MessageUtils;
import com.kairos.utils.RepeatingTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class PurgeDisaster extends ActiveDisaster {

    protected BossBar bossBar;
    protected int maxMobs;
    protected int spawnedMobs = 0;
    protected Set<LivingEntity> activeMobs = new HashSet<>();
    private RepeatingTask purgeTask;

    public PurgeDisaster(DisasterType type, int level, World world) {
        super(type, level, world);
        this.maxMobs = getIntOrDefault(type.getId() + ".max_mobs.level_" + level, 10 + (level * 5));
    }

    // Child classes ONLY need to provide these!
    protected abstract void spawnDefaultMob(Location loc);
    protected void onMobSpawned(LivingEntity entity) {}

    protected void spawnMob(Location loc) {
        ConfigurationSection mobsConfig = configFile.getConfigurationSection(getType().getId() + ".mobs");
        
        if (mobsConfig == null || mobsConfig.getKeys(false).isEmpty()) {
            spawnDefaultMob(loc); // Config is missing/broken, use the child's fallback!
            return;
        }

        int totalWeight = 0;
        for (String key : mobsConfig.getKeys(false)) {
            totalWeight += mobsConfig.getInt(key);
        }

        if (totalWeight <= 0) {
            spawnDefaultMob(loc);
            return;
        }

        int randomValue = new Random().nextInt(totalWeight);
        int currentWeight = 0;
        String selectedMob = null;

        for (String key : mobsConfig.getKeys(false)) {
            currentWeight += mobsConfig.getInt(key);
            if (randomValue < currentWeight) {
                selectedMob = key;
                break;
            }
        }

        if (selectedMob == null) return;

        LivingEntity spawned = null;
        CustomEntityType customType = CustomEntityType.getCustomEntityType(selectedMob);
        
        if (customType != null) {
            spawned = customType.spawn(loc).getBukkitEntity();
        } else {
            try {
                EntityType vanillaType = EntityType.valueOf(selectedMob.toUpperCase());
                spawned = (LivingEntity) world.spawnEntity(loc, vanillaType);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid mob type in " + getType().getId() + " config: " + selectedMob);
            }
        }

        if (spawned != null) {
            onMobSpawned(spawned);
            activeMobs.add(spawned);
        }
    }

    @Override
    public void start(World world, Player p, boolean broadcastAllowed) {
        this.target = p;
        ongoingDisasters.add(this);

        triggerStartCommands(p); // Fires commands from disasters.yml!

        String barTitle = configFile.getString(getType().getId() + ".bossbar.title", "&c" + getType().getDisplayName());
        BarColor barColor;
        try { barColor = BarColor.valueOf(configFile.getString(getType().getId() + ".bossbar.color", "RED").toUpperCase()); } 
        catch (IllegalArgumentException e) { barColor = BarColor.RED; }

        this.bossBar = Bukkit.createBossBar(ColorUtils.colorize(barTitle), barColor, BarStyle.SOLID);
        this.bossBar.addPlayer(p);

        if (broadcastAllowed && isWarningEnabled()) {
            MessageUtils.send(p, "messages.purge.target_" + getType().getId(), "&c" + barTitle + " &7has targeted you!");
            p.playSound(p.getLocation(), org.bukkit.Sound.EVENT_RAID_HORN, 1.0f, 1.0f);
        }

        this.purgeTask = new RepeatingTask(plugin, 40, 20) { 
            @Override
            public void run() {
                if (target == null || !target.isOnline() || target.isDead() || !target.getWorld().equals(world)) {
                    endPurge(false);
                    return;
                }

                activeMobs.removeIf(LivingEntity::isDead);

                double progress = 1.0 - ((double) (spawnedMobs - activeMobs.size()) / maxMobs);
                bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));

                if (spawnedMobs >= maxMobs && activeMobs.isEmpty()) {
                    endPurge(true);
                    return;
                }

                if (spawnedMobs < maxMobs && activeMobs.size() < 12) {
                    Location spawnLoc = LocationUtils.getRandomSurfaceLocation(target.getLocation(), 25);
                    if (spawnLoc != null && spawnLoc.distanceSquared(target.getLocation()) > 100) {
                        spawnMob(spawnLoc);
                        spawnedMobs++;
                    }
                }
                
                for (LivingEntity mob : activeMobs) {
                    if (mob instanceof Mob m && m.getTarget() == null) {
                        m.setTarget(target);
                    }
                }
            }
        };
    }

    protected void endPurge(boolean victory) {
        triggerEndCommands(); // Fires commands from disasters.yml!
        ongoingDisasters.remove(this);
        if (bossBar != null) bossBar.removeAll();
        if (purgeTask != null) purgeTask.cancel();
        
        if (victory && target != null && target.isOnline()) {
            String endMsg = configFile.getString(getType().getId() + ".bossbar.end_message", "&aYou survived!");
            MessageUtils.send(target, "messages.purge.victory_" + getType().getId(), endMsg);
            target.playSound(target.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        for (LivingEntity e : activeMobs) {
            if (e != null && e.isValid()) e.remove();
        }
        activeMobs.clear();
    }

    @Override
    public void clear() {
        endPurge(false);
    }
}
