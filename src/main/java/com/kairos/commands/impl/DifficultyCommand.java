package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.MessageUtils;
import com.kairos.world.Difficulty;
import com.kairos.world.WorldProfile;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DifficultyCommand implements SubCommand {

    private final KairosPlugin plugin;
    public DifficultyCommand(KairosPlugin plugin) { this.plugin = plugin; }

    @Override public String getName() { return "difficulty"; }
    @Override public String getDescription() { return "Change a world's disaster difficulty."; }
    @Override public String getSyntax() { return "/disasters difficulty <world> <difficulty>"; }
    @Override public String getPermission() { return "kairos.admin.difficulty"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtils.send(sender, "messages.errors.usage", "&cUsage: %syntax%", "%syntax%", getSyntax());
            return;
        }

        World world = Bukkit.getWorld(args[1]);
        if (world == null && !args[1].equalsIgnoreCase("this_world")) {
            MessageUtils.send(sender, "messages.errors.invalid_world", "&cWorld not found!");
            return;
        }
        
        if (args[1].equalsIgnoreCase("this_world") && sender instanceof Player p) {
            world = p.getWorld();
        }

        Difficulty difficulty;
        try {
            difficulty = Difficulty.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            MessageUtils.send(sender, "messages.errors.invalid_difficulty", "&cInvalid difficulty! Options: EASY, NORMAL, HARD, EXTREME, CUSTOM");
            return;
        }

        WorldProfile profile = plugin.getWorldManager().getProfile(world);
        if (profile != null) {
            profile.setDifficulty(difficulty);
            // GAP PATCHED: Saves the profile to disk so it persists after restart!
            plugin.getWorldManager().saveProfile(profile); 
            
            MessageUtils.send(sender, "messages.admin.difficulty_set", "&aDifficulty for &e%world% &aset to &e%diff%&a!", 
                    "%world%", world.getName(), "%diff%", difficulty.name());
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            List<String> worlds = new ArrayList<>(Bukkit.getWorlds().stream().map(World::getName).toList());
            worlds.add("this_world");
            StringUtil.copyPartialMatches(args[1], worlds, completions);
        } else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], Arrays.stream(Difficulty.values()).map(Enum::name).toList(), completions);
        }
        return completions;
    }
}