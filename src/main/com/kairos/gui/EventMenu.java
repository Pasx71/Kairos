package com.kairos.gui;

import com.kairos.KairosPlugin;
import com.kairos.events.base.ActiveDisaster;
import com.kairos.utils.ColorUtils;
import com.kairos.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class EventMenu implements InventoryHolder {

    private final Inventory inventory;
    private final KairosPlugin plugin;
    private final FileConfiguration config;

    public EventMenu(KairosPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getConfig("gui.yml");
        
        String title = config.getString("events_menu.title", "&8Ongoing Events");
        int size = config.getInt("events_menu.size", 27);
        
        this.inventory = Bukkit.createInventory(this, size, ColorUtils.colorize(title));
        initializeItems();
    }

    private void initializeItems() {
        // Fetch Filler Material
        Material fillerMat = Material.matchMaterial(config.getString("events_menu.items.filler.material", "GRAY_STAINED_GLASS_PANE"));
        if (fillerMat == null) fillerMat = Material.GRAY_STAINED_GLASS_PANE;
        
        ItemStack filler = new ItemBuilder(fillerMat).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        int slot = config.getInt("events_menu.items.events.start_slot", 10); 
        
        if (ActiveDisaster.ongoingDisasters.isEmpty()) {
            Material emptyMat = Material.matchMaterial(config.getString("events_menu.items.empty.material", "BARRIER"));
            String emptyName = config.getString("events_menu.items.empty.name", "&cNo Active Disasters");
            String emptyLore = config.getString("events_menu.items.empty.lore", "&7The world is currently peaceful.");
            
            ItemStack noEvents = new ItemBuilder(emptyMat == null ? Material.BARRIER : emptyMat)
                    .name(emptyName)
                    .lore(emptyLore)
                    .build();
            
            inventory.setItem(config.getInt("events_menu.items.empty.slot", 13), noEvents);
        } else {
            for (ActiveDisaster disaster : ActiveDisaster.ongoingDisasters) {
                if (slot > config.getInt("events_menu.items.events.end_slot", 16)) break; 
                
                ItemStack icon = new ItemBuilder(Material.LAVA_BUCKET) 
                        .name("&e" + disaster.getType().getDisplayName())
                        .lore(
                            "&7Level: &c" + disaster.getLevel(),
                            "&7World: &f" + disaster.getWorld().getName()
                        ).build();
                
                inventory.setItem(slot, icon);
                slot++;
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleClick(Player player, int slot, ItemStack clickedItem) {
        Material emptyMat = Material.matchMaterial(config.getString("events_menu.items.empty.material", "BARRIER"));
        if (clickedItem != null && clickedItem.getType() == (emptyMat != null ? emptyMat : Material.BARRIER)) {
            player.sendMessage(ColorUtils.colorize("&cThere are no events to manage right now."));
            player.closeInventory();
        }
    }
}