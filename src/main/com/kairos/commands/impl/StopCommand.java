package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.api.DisasterType;
import com.kairos.commands.framework.SubCommand;
import com.kairos.events.base.ActiveDisaster;
import com.kairos.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class StopCommand implements SubCommand {

    private final KairosPlugin plugin;
    public StopCommand(KairosPlugin plugin) { this.plugin = plugin; }

    @Override public String getName() { return "stop"; }
    @Override public String getDescription() { return "Forcefully stops active disasters."; }
    @Override public String getSyntax() { return "/disasters stop <all|disaster_name>"; }
    @Override public String getPermission() { return "kairos.admin.stop"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.send(sender, "messages.errors.usage", "&cUsage: %syntax%", "%syntax%", getSyntax());
            return;
        }

        String target = args[1].toLowerCase();
        int stopped = 0;

        if (target.equals("all")) {
            for (ActiveDisaster d : new ArrayList<>(ActiveDisaster.ongoingDisasters)) {
                d.clear(); // Clears tasks and removes from list
                stopped++;
            }
        } else {
            DisasterType type = DisasterType.forName(target);
            if (type == null) {
                MessageUtils.send(sender, "messages.errors.invalid_disaster", "&cUnknown disaster: %disaster%", "%disaster%", target);
                return;
            }

            for (ActiveDisaster d : new ArrayList<>(ActiveDisaster.ongoingDisasters)) {
                if (d.getType().equals(type)) {
                    d.clear();
                    stopped++;
                }
            }
        }

        MessageUtils.send(sender, "messages.admin.stop_success", "&aSuccessfully stopped &e%amount% &aactive disasters.", "%amount%", String.valueOf(stopped));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            List<String> options = new ArrayList<>();
            options.add("all");
            for (DisasterType type : DisasterType.values()) options.add(type.getId());
            StringUtil.copyPartialMatches(args[1], options, completions);
        }
        return completions;
    }
}