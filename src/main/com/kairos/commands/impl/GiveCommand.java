package com.kairos.commands.impl;

import com.kairos.KairosPlugin;
import com.kairos.api.items.CustomItem;
import com.kairos.commands.framework.SubCommand;
import com.kairos.utils.MessageUtils;
import com.kairos.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class GiveCommand implements SubCommand {

    private final KairosPlugin plugin;
    public GiveCommand(KairosPlugin plugin) { this.plugin = plugin; }

    @Override public String getName() { return "give"; }
    @Override public String getDescription() { return "Give a custom Kairos item."; }
    @Override public String getSyntax() { return "/disasters give <item_id> [player] [amount]"; }
    @Override public String getPermission() { return "kairos.admin.give"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.send(sender, "messages.errors.usage", "&cUsage: %syntax%", "%syntax%", getSyntax());
            return;
        }

        String itemId = args[1].toLowerCase();
        CustomItem customItem = plugin.getItemRegistry().getItemById(itemId);

        if (customItem == null) {
            MessageUtils.send(sender, "messages.errors.invalid_item", "&cUnknown item: %item%", "%item%", itemId);
            return;
        }

        Player target = args.length >= 3 ? Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);
        if (target == null) {
            MessageUtils.send(sender, "messages.errors.invalid_player", "&cPlayer not found!");
            return;
        }

        int amount = args.length >= 4 ? NumberUtils.parseIntOrDefault(args[3], 1) : 1;

        ItemStack itemStack = customItem.buildItem();
        itemStack.setAmount(amount);

        // Safely add to inventory or drop on the ground if full
        target.getInventory().addItem(itemStack).values().forEach(leftover -> 
            target.getWorld().dropItem(target.getLocation(), leftover)
        );

        MessageUtils.send(sender, "messages.admin.give_success", "&aGave %amount%x %item% to %player%.", 
                "%amount%", String.valueOf(amount), "%item%", itemId, "%player%", target.getName());
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], plugin.getItemRegistry().getRegisteredIds(), completions);
        } else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), completions);
        }
        return completions;
    }
}