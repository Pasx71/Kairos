package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.MessageUtils;
import com.kairos.world.WorldProfile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WhitelistCommand implements SubCommand {

    private final KairosPlugin plugin;
    public WhitelistCommand(KairosPlugin plugin) { this.plugin = plugin; }

    @Override public String getName() { return "whitelist"; }
    @Override public String getDescription() { return "Whitelist a player from random disasters."; }
    @Override public String getSyntax() { return "/disasters whitelist <add|remove> <player>[world]"; }
    @Override public String getPermission() { return "kairos.admin.whitelist"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtils.send(sender, "messages.errors.usage", "&cUsage: %syntax%", "%syntax%", getSyntax());
            return;
        }

        String action = args[1].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        
        World world = sender instanceof Player p ? p.getWorld() : Bukkit.getWorlds().get(0);
        if (args.length == 4) {
            world = Bukkit.getWorld(args[3]);
            if (world == null) {
                MessageUtils.send(sender, "messages.errors.invalid_world", "&cWorld not found!");
                return;
            }
        }

        WorldProfile profile = plugin.getWorldManager().getProfile(world);
        if (profile == null) return;

        if (action.equals("add")) {
            profile.addWhitelistedPlayer(target.getUniqueId());
            plugin.getWorldManager().saveProfile(profile); // Save to disk
            MessageUtils.send(sender, "messages.admin.whitelist_add", "&aAdded &e%player% &ato the disaster whitelist in &e%world%", "%player%", target.getName(), "%world%", world.getName());
        } else if (action.equals("remove")) {
            profile.removeWhitelistedPlayer(target.getUniqueId());
            plugin.getWorldManager().saveProfile(profile); // Save to disk
            MessageUtils.send(sender, "messages.admin.whitelist_remove", "&cRemoved &e%player% &cfrom the disaster whitelist in &e%world%", "%player%", target.getName(), "%world%", world.getName());
        } else {
            MessageUtils.send(sender, "messages.errors.usage", "&cUsage: %syntax%", "%syntax%", getSyntax());
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], Arrays.asList("add", "remove"), completions);
        } else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), completions);
        } else if (args.length == 4) {
            StringUtil.copyPartialMatches(args[3], Bukkit.getWorlds().stream().map(World::getName).toList(), completions);
        }
        return completions;
    }
}