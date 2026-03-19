package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.ColorUtils;
import com.kairos.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SummonCommand implements SubCommand {

    private final KairosPlugin plugin;
    public SummonCommand(KairosPlugin plugin) { this.plugin = plugin; }

    @Override public String getName() { return "summon"; }
    @Override public String getDescription() { return "Summon a custom entity."; }
    @Override public String getSyntax() { return "/disasters summon <entityType> [amount] [world]"; }
    @Override public String getPermission() { return "kairos.summon"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        CustomEntityType type = CustomEntityType.getCustomEntityType(args[1]);
        if (type == null) {
            sender.sendMessage(ColorUtils.colorize("&cNo such entity type: " + args[1]));
            return;
        }

        int amount = args.length >= 3 ? NumberUtils.parseIntOrDefault(args[2], 1) : 1;
        World targetWorld;
        
        if (args.length >= 4) {
            targetWorld = Bukkit.getWorld(args[3]);
        } else {
            targetWorld = sender instanceof Player ? ((Player) sender).getWorld() : Bukkit.getWorlds().get(0);
        }

        if (targetWorld == null) {
            sender.sendMessage(ColorUtils.colorize("&cWorld not found!"));
            return;
        }

        Location loc = sender instanceof Player ? ((Player) sender).getLocation() : targetWorld.getSpawnLocation();

        // MISSING LOGIC PATCHED: Uses proper Type Blueprint spawning
        for (int i = 0; i < amount; i++) {
            type.spawn(loc);
        }

        sender.sendMessage(ColorUtils.colorize("&aSpawned " + amount + " &" + type.getColChar() + type.species + "&a in world " + targetWorld.getName() + "!"));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], CustomEntityType.speciesList, completions);
        } else if (args.length == 4) {
            StringUtil.copyPartialMatches(args[3], Bukkit.getServer().getWorlds().stream().map(World::getName).toList(), completions);
        }
        Collections.sort(completions);
        return completions;
    }
}