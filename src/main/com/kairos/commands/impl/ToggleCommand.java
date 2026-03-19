package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.api.DisasterType;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.MessageUtils;
import com.kairos.world.WorldProfile;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToggleCommand implements SubCommand {

    private final KairosPlugin plugin;
    public ToggleCommand(KairosPlugin plugin) { this.plugin = plugin; }

    @Override public String getName() { return "toggle"; }
    @Override public String getDescription() { return "Toggle natural spawns for a disaster."; }
    @Override public String getSyntax() { return "/disasters toggle <random_spawns|disaster_name> [world]"; }
    @Override public String getPermission() { return "kairos.admin.toggle"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.send(sender, "messages.errors.usage", "&cUsage: %syntax%", "%syntax%", getSyntax());
            return;
        }

        World world = sender instanceof Player p ? p.getWorld() : Bukkit.getWorlds().get(0);
        if (args.length == 3) {
            world = Bukkit.getWorld(args[2]);
            if (world == null) {
                MessageUtils.send(sender, "messages.errors.invalid_world", "&cWorld not found!");
                return;
            }
        }

        WorldProfile profile = plugin.getWorldManager().getProfile(world);
        if (profile == null) return;

        String target = args[1].toLowerCase();

        if (target.equals("random_spawns")) {
            boolean newState = !profile.isNaturalSpawningAllowed();
            profile.setNaturalSpawningAllowed(newState);
            
            // GAP PATCHED: Saves to worlds.yml
            plugin.getWorldManager().saveProfile(profile); 
            
            String stateStr = newState ? "&a&lON" : "&c&lOFF";
            MessageUtils.send(sender, "messages.admin.toggle_random", "&eRandom Disasters &ain &e%world% &aare now: %state%",
                    "%world%", world.getName(), "%state%", stateStr);
            return;
        }

        DisasterType type = DisasterType.forName(target);
        if (type != null) {
            boolean isAllowed = profile.canSpawn(type);
            if (isAllowed) {
                profile.removeAllowedDisaster(type);
                plugin.getWorldManager().saveProfile(profile); 
                MessageUtils.send(sender, "messages.admin.toggle_disaster", "&e%disaster% &a natural spawns are now: &c&lOFF", "%disaster%", type.getDisplayName());
            } else {
                profile.addAllowedDisaster(type);
                plugin.getWorldManager().saveProfile(profile); 
                MessageUtils.send(sender, "messages.admin.toggle_disaster", "&e%disaster% &a natural spawns are now: &a&lON", "%disaster%", type.getDisplayName());
            }
        } else {
            MessageUtils.send(sender, "messages.errors.unknown_disaster", "&cUnknown disaster or setting: %target%", "%target%", target);
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            List<String> options = new ArrayList<>(DisasterType.values().stream().map(DisasterType::getId).toList());
            options.add("random_spawns");
            StringUtil.copyPartialMatches(args[1], options, completions);
        } else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], Bukkit.getWorlds().stream().map(World::getName).toList(), completions);
        }
        return completions;
    }
}