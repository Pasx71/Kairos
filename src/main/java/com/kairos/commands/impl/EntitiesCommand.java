package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.api.entities.CustomEntity;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.ColorUtils;
import com.kairos.utils.NumberUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.*;

public class EntitiesCommand implements SubCommand {

    private final KairosPlugin plugin;
    public EntitiesCommand(KairosPlugin plugin) { this.plugin = plugin; }

    @Override public String getName() { return "entities"; }
    @Override public String getDescription() { return "Manage spawned custom entities."; }
    @Override public String getSyntax() { return "/disasters entities <list|kill> [type]"; }
    @Override public String getPermission() { return "kairos.entities"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "list":
                handleList(sender, args);
                break;
            case "kill":
                handleKill(sender, args);
                break;
            default:
                sender.sendMessage(ColorUtils.colorize("&cUnknown subcommand: " + args[1]));
        }
    }

    private void handleList(CommandSender sender, String[] args) {
        if (args.length >= 3) {
            String targetId = args[2].toLowerCase();
            
            // GAP PATCHED: Utilizing your new Registry!
            for (CustomEntity e : plugin.getEntityRegistry().getActiveEntities()) {
                if (e.getBukkitEntity() != null && e.getId().equals(targetId)) {
                    sendEntityInfo(sender, e);
                }
            }
        } else {
            sender.sendMessage(ColorUtils.colorize("&aCurrently spawned entities:"));
            Map<String, Integer> countMap = new HashMap<>();
            for (CustomEntity e : plugin.getEntityRegistry().getActiveEntities()) {
                if (e.getBukkitEntity() != null) {
                    countMap.put(e.getId(), countMap.getOrDefault(e.getId(), 0) + 1);
                }
            }
            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                sender.sendMessage(ColorUtils.colorize("&3- &b" + entry.getKey() + " &7- &f" + entry.getValue()));
            }
        }
    }

    private void sendEntityInfo(CommandSender sender, CustomEntity e) {
        double health = e.getBukkitEntity().getHealth();
        // Fallback max health check using Bukkit attribute
        double maxHealth = e.getBukkitEntity().getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        String healthColor = health < maxHealth / 4.0 ? "c" : (health < maxHealth / 2.0 ? "e" : "a");
        String healthString = '(' + NumberUtils.formatDouble(health) + '/' + NumberUtils.formatDouble(maxHealth) + ')';

        String name = (e.getBukkitEntity().getCustomName() != null ? e.getBukkitEntity().getCustomName() : e.getId());
        Location loc = e.getBukkitEntity().getLocation();

        String formatted = ColorUtils.colorize("&3- &b" + name + " &" + healthColor + healthString + " &7- &6(" + loc.getWorld().getName() + ") &f" +
                loc.getBlockX() + ' ' + loc.getBlockY() + ' ' + loc.getBlockZ());
        
        sender.sendMessage(formatted);
    }

    private void handleKill(CommandSender sender, String[] args) {
        int killed = 0;
        if (args.length >= 3) {
            String targetId = args[2].toLowerCase();
            for (CustomEntity e : plugin.getEntityRegistry().getActiveEntities()) {
                if (e.getBukkitEntity() != null && e.getId().equals(targetId)) {
                    e.getBukkitEntity().remove();
                    killed++;
                }
            }
            sender.sendMessage(ColorUtils.colorize("&eKilled " + killed + " &b" + targetId + "&e!"));
        } else {
            for (CustomEntity e : plugin.getEntityRegistry().getActiveEntities()) {
                if (e.getBukkitEntity() != null) {
                    e.getBukkitEntity().remove();
                    killed++;
                }
            }
            sender.sendMessage(ColorUtils.colorize("&eKilled " + killed + " custom entities!"));
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) completions.addAll(Arrays.asList("list", "kill"));
        return completions;
    }
}