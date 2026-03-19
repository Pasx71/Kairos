package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.ColorUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand implements SubCommand {

    private final KairosPlugin plugin;

    public ReloadCommand(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin configurations.";
    }

    @Override
    public String getSyntax() {
        return "/disasters reload";
    }

    @Override
    public String getPermission() {
        return "kairos.admin.reload";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        // Reload standard config.yml using Bukkit's built in system
        plugin.reloadConfig();

        // Reload all internal configuration files (disasters, mobs, etc.)
        plugin.getConfigManager().reloadConfigs();
        
        // Reload data/worlds files
        plugin.getDataFileManager().loadConfig();
        
        // Refresh registries and caches
        plugin.getDisasterRegistry().reloadSettings();
        com.kairos.world.Difficulty.loadConfig(plugin.getConfigManager().getConfig("config.yml"));
        
        sender.sendMessage(ColorUtils.colorize("&8[&bKairos&8] &aAll configurations have been successfully reloaded!"));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}