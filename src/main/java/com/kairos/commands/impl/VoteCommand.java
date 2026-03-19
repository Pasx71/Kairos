package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.api.DisasterType;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoteCommand implements SubCommand {

    private final KairosPlugin plugin;
    public VoteCommand(KairosPlugin plugin) { this.plugin = plugin; }

    @Override public String getName() { return "vote"; }
    @Override public String getDescription() { return "Vote on a disaster type."; }
    @Override public String getSyntax() { return "/disasters vote <favor|dislike> <disaster>"; }
    @Override public String getPermission() { return "kairos.vote"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtils.send(sender, "messages.errors.usage", "&cUsage: %syntax%", "%syntax%", getSyntax());
            return;
        }

        String action = args[1].toLowerCase();
        if (!action.equals("favor") && !action.equals("dislike")) {
            MessageUtils.send(sender, "messages.errors.unknown_action", "&cUnknown action: %action%", "%action%", args[1]);
            return;
        }

        DisasterType disaster = DisasterType.forName(args[2]);
        if (disaster == null) {
            MessageUtils.send(sender, "messages.errors.invalid_disaster", "&cInvalid disaster name!");
            return;
        }

        // GAP PATCHED: Used a dedicated DataFileManager to safely store votes
        plugin.getDataFileManager().getConfig().set("votes." + action + "." + disaster.getId() + "." + sender.getName(), System.currentTimeMillis());
        plugin.getDataFileManager().saveConfig();

        MessageUtils.send(sender, "messages.vote_success", "&aYour vote for %disaster% has been submitted!", "%disaster%", disaster.getDisplayName());
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], Arrays.asList("favor", "dislike"), completions);
        } else if (args.length == 3) {
            List<String> ids = DisasterType.values().stream().map(DisasterType::getId).toList();
            StringUtil.copyPartialMatches(args[2], ids, completions);
        }
        return completions;
    }
}