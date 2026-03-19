package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.api.seasons.Season;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeasonCommand implements SubCommand {

    private final KairosPlugin plugin;
    public SeasonCommand(KairosPlugin plugin) { this.plugin = plugin; }

    @Override public String getName() { return "season"; }
    @Override public String getDescription() { return "Check or change the current season."; }
    @Override public String getSyntax() { return "/disasters season <info|set> [season_name] [world]"; }
    @Override public String getPermission() { return "kairos.admin.season"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        World world = sender instanceof Player p ? p.getWorld() : Bukkit.getWorlds().get(0);
        
        if (args[1].equalsIgnoreCase("info")) {
            Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);
            int daysPassed = plugin.getSeasonManager().getDaysPassed(world);
            int targetDuration = plugin.getSeasonManager().getTargetDuration(world);
            
            sender.sendMessage(ColorUtils.colorize("&8=== &bKairos Seasons &8==="));
            sender.sendMessage(ColorUtils.colorize("&7World: &e" + world.getName()));
            sender.sendMessage(ColorUtils.colorize("&7Current Season: " + currentSeason.getDisplayName()));
            sender.sendMessage(ColorUtils.colorize("&7Progress: &f" + daysPassed + " &7/ &f" + targetDuration + " days"));
            return;
        }

        if (args[1].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtils.colorize("&cPlease specify a season: SPRING, SUMMER, AUTUMN, WINTER"));
                return;
            }

            if (args.length == 4) {
                world = Bukkit.getWorld(args[3]);
                if (world == null) {
                    sender.sendMessage(ColorUtils.colorize("&cWorld not found!"));
                    return;
                }
            }

            try {
                Season newSeason = Season.valueOf(args[2].toUpperCase());
                plugin.getSeasonManager().setSeason(world, newSeason);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ColorUtils.colorize("&cInvalid season! Use SPRING, SUMMER, AUTUMN, or WINTER."));
            }
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], Arrays.asList("info", "set"), completions);
        } else if (args.length == 3 && args[1].equalsIgnoreCase("set")) {
            StringUtil.copyPartialMatches(args[2], Arrays.stream(Season.values()).map(Enum::name).toList(), completions);
        } else if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
            StringUtil.copyPartialMatches(args[3], Bukkit.getWorlds().stream().map(World::getName).toList(), completions);
        }
        return completions;
    }
}