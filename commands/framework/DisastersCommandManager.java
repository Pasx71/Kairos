package com.kairos.commands.framework;

import com.kairos.KairosPlugin;
import com.kairos.commands.impl.*;
import com.kairos.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class DisastersCommandManager implements CommandExecutor, TabCompleter {

    private final List<SubCommand> subcommands = new ArrayList<>();

    public DisastersCommandManager(KairosPlugin plugin) {
        subcommands.add(new StartCommand(plugin));
        subcommands.add(new SummonCommand(plugin));
        subcommands.add(new EntitiesCommand(plugin));
        subcommands.add(new VoteCommand(plugin));
        subcommands.add(new EventCommand(plugin));
        subcommands.add(new DifficultyCommand(plugin));
        subcommands.add(new WhitelistCommand(plugin));
        subcommands.add(new ToggleCommand(plugin));
        subcommands.add(new GiveCommand(plugin));
        subcommands.add(new SeasonCommand(plugin));
        subcommands.add(new ReloadCommand(plugin));
        subcommands.add(new StopCommand(plugin));
        subcommands.add(new DiseaseCommand(plugin));

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            for (SubCommand subcommand : subcommands) {
                if (args[0].equalsIgnoreCase(subcommand.getName())) {
                    if (subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) {
                        sender.sendMessage(ColorUtils.colorize("&cYou do not have permission!"));
                        return true;
                    }
                    subcommand.perform(sender, args);
                    return true;
                }
            }
        }
        sender.sendMessage(ColorUtils.colorize("&cUsage: /disasters <start|summon|entities|vote>"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (SubCommand sub : subcommands) {
                if (sub.getPermission() == null || sender.hasPermission(sub.getPermission())) {
                    if (sub.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(sub.getName());
                    }
                }
            }
            return completions;
        }
        for (SubCommand sub : subcommands) {
            if (args[0].equalsIgnoreCase(sub.getName())) {
                return sub.getTabCompletions(sender, args);
            }
        }
        return completions;
    }
}