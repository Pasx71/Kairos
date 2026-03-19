package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.api.DisasterType;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.MessageUtils;
import com.kairos.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StartCommand implements SubCommand {

    private final KairosPlugin plugin;
    public StartCommand(KairosPlugin plugin) { this.plugin = plugin; }

    @Override public String getName() { return "start"; }
    @Override public String getDescription() { return "Force start a disaster."; }
    @Override public String getSyntax() { return "/disasters start <disaster> [level] [world] [player] [notify]"; }
    @Override public String getPermission() { return "kairos.start"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.send(sender, "messages.errors.usage", "&cUsage: %syntax%", "%syntax%", getSyntax());
            return;
        }

        DisasterType type = DisasterType.forName(args[1]);
        if (type == null) {
            MessageUtils.send(sender, "messages.errors.invalid_disaster", "&cUnknown disaster: %disaster%", "%disaster%", args[1]);
            return;
        }

        int level = args.length >= 3 ? NumberUtils.parseIntOrDefault(args[2], 1) : 1;
        
        // Resolve target world and player safely
        Player targetPlayer = args.length >= 5 ? Bukkit.getPlayer(args[4]) : (sender instanceof Player ? (Player) sender : null);
        World world;
        if (args.length >= 4 && !args[3].equalsIgnoreCase("this_world")) {
            world = Bukkit.getWorld(args[3]);
        } else {
            world = targetPlayer != null ? targetPlayer.getWorld() : Bukkit.getWorlds().get(0);
        }

        if (world == null) {
            MessageUtils.send(sender, "messages.errors.invalid_world", "&cWorld not found!");
            return;
        }

        boolean notify = args.length >= 6 && args[5].equalsIgnoreCase("true");

        // GAP PATCHED: Actually trigger the disaster using the Registry
        plugin.getDisasterRegistry().startDisaster(type, level, world, targetPlayer, notify);

        MessageUtils.send(sender, "messages.admin.disaster_started", "&aDisaster &e%disaster%&a started in &e%world%&a!", 
                "%disaster%", type.getDisplayName(), "%world%", world.getName());
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            List<String> names = DisasterType.values().stream().map(DisasterType::getId).toList();
            StringUtil.copyPartialMatches(args[1], names, completions);
        } else if (args.length == 3) {
            completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "6"));
        } else if (args.length == 4) {
            List<String> worlds = new ArrayList<>(Bukkit.getWorlds().stream().map(World::getName).toList());
            worlds.add("this_world");
            StringUtil.copyPartialMatches(args[3], worlds, completions);
        }
        Collections.sort(completions);
        return completions;
    }
}