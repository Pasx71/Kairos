package com.kairos.events.base;

import com.kairos.api.DisasterType;
import com.kairos.world.WorldProfile;
import com.kairos.KairosPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public abstract class ActiveDisaster {

    protected final KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
    protected DisasterType type; 
    protected int level;
    protected World world;
    protected WorldProfile worldProfile; 
    protected FileConfiguration configFile;
    protected Player target; // Track the targeted player for commands

    public static Queue<ActiveDisaster> ongoingDisasters = new ArrayDeque<>();
    public static Map<UUID, Map<ActiveDisaster, Integer>> countdownMap = new HashMap<>();

    public ActiveDisaster(DisasterType type, int level, World world) {
        this.type = type;
        this.level = level;
        this.world = world;
        this.worldProfile = plugin.getWorldManager().getProfile(world);
        this.configFile = plugin.getConfigManager().getConfig("disasters.yml");
    }

    // --- NEW MODULAR COMMAND SYSTEM ---
    public void executeConfigCommands(String pathType) {
        List<String> commands = configFile.getStringList(type.getId() + "." + pathType);
        if (commands == null || commands.isEmpty()) return;

        String playerName = (target != null) ? target.getName() : "";
        String worldName = world.getName();

        for (String cmd : commands) {
            String parsedCmd = cmd.replace("%player%", playerName)
                                  .replace("%world%", worldName)
                                  .replace("%level%", String.valueOf(level));
            
            // Dispatch via the server console
            Bukkit.getScheduler().runTask(plugin, () -> 
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCmd)
            );
        }
    }

    // Call this inside your child classes right after adding to 'ongoingDisasters'
    public void triggerStartCommands(Player target) {
        this.target = target;
        executeConfigCommands("commands_on_start");
    }

    // Call this inside your child classes right before they cancel their tasks
    public void triggerEndCommands() {
        executeConfigCommands("commands_on_end");
    }
    // -----------------------------------

    protected int getIntOrDefault(String path, int fallback) {
        return configFile.contains(path) ? configFile.getInt(path) : fallback;
    }

    protected double getDoubleOrDefault(String path, double fallback) {
        return configFile.contains(path) ? configFile.getDouble(path) : fallback;
    }

    protected boolean getBooleanOrDefault(String path, boolean fallback) {
        return configFile.contains(path) ? configFile.getBoolean(path) : fallback;
    }

    public boolean isWarningEnabled() {
        return getBooleanOrDefault(type.getId() + ".send_warning", true);
    }

    public void inputPlayerToMap(int seconds, Player player) {
        countdownMap.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(this, seconds);
    }

    public boolean isEntityTypeProtected(Entity entity) {
        if (worldProfile == null) return false;
        return worldProfile.getBlacklistedEntities().contains(entity.getType());
    }

    public DisasterType getType() { return type; }
    public int getLevel() { return level; }
    public World getWorld() { return world; }
    
    public abstract void start(World world, Player player, boolean broadcastAllowed);
    public abstract void clear();
}
