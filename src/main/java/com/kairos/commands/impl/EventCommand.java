package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.ColorUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EventCommand implements SubCommand {

    private final KairosPlugin plugin;

    public EventCommand(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "event";
    }

    @Override
    public String getDescription() {
        return "Opens the current event GUI.";
    }

    @Override
    public String getSyntax() {
        return "/disasters event";
    }

    @Override
    public String getPermission() {
        return "kairos.event";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            String msg = plugin.getConfig().getString(
                    "messages.console_error_message",
                    "&cOnly players can use this!"
            );
            sender.sendMessage(ColorUtils.colorize(msg));
            return;
        }

        // Use a getter instead of direct field access
        if (!plugin.getEventHandler().isEnabled()) {
            player.sendMessage(ColorUtils.colorize("&cThere is no ongoing event at this time!"));
            return;
        }

        plugin.getEventHandler().openGUI(player);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}