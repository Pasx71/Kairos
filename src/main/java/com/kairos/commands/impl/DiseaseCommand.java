package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.api.disease.Disease;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.ColorUtils;
import com.kairos.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiseaseCommand implements SubCommand {

    private final KairosPlugin plugin;
    public DiseaseCommand(KairosPlugin plugin) { this.plugin = plugin; }

    @Override public String getName() { return "disease"; }
    @Override public String getDescription() { return "Infect or cure a player."; }
    @Override public String getSyntax() { return "/disasters disease <infect|cure|clear> <player> [disease]"; }
    @Override public String getPermission() { return "kairos.admin.disease"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtils.send(sender, "messages.errors.usage", "&cUsage: %syntax%", "%syntax%", getSyntax());
            return;
        }

        String action = args[1].toLowerCase();
        Player target = Bukkit.getPlayer(args[2]);

        if (target == null) {
            MessageUtils.send(sender, "messages.errors.invalid_player", "&cPlayer not found!");
            return;
        }

        if (action.equals("clear")) {
            for (Disease d : plugin.getDiseaseManager().getActiveDiseases(target)) {
                plugin.getDiseaseManager().cure(target, d);
            }
            sender.sendMessage(ColorUtils.colorize("&aCleared all diseases from " + target.getName()));
            return;
        }

        if (args.length < 4) {
            MessageUtils.send(sender, "messages.errors.usage", "&cUsage: %syntax%", "%syntax%", getSyntax());
            return;
        }

        Disease disease = plugin.getDiseaseManager().getDisease(args[3].toLowerCase());
        if (disease == null) {
            sender.sendMessage(ColorUtils.colorize("&cUnknown disease ID!"));
            return;
        }

        if (action.equals("infect")) {
            plugin.getDiseaseManager().infect(target, disease);
            sender.sendMessage(ColorUtils.colorize("&aInfected " + target.getName() + " with " + disease.getDisplayName()));
        } else if (action.equals("cure")) {
            plugin.getDiseaseManager().cure(target, disease);
            sender.sendMessage(ColorUtils.colorize("&aCured " + target.getName() + " of " + disease.getDisplayName()));
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], Arrays.asList("infect", "cure", "clear"), completions);
        } else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), completions);
        } else if (args.length == 4 && !args[1].equalsIgnoreCase("clear")) {
            List<String> diseaseIds = plugin.getDiseaseManager().getAllDiseases().stream().map(Disease::getId).collect(Collectors.toList());
            StringUtil.copyPartialMatches(args[3], diseaseIds, completions);
        }
        return completions;
    }
}