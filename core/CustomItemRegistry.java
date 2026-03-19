package com.kairos.core;

import com.kairos.api.items.CustomItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class CustomItemRegistry {

    private final Plugin plugin;
    private final Map<String, CustomItem> registeredItems = new HashMap<>();

    public CustomItemRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerItem(CustomItem item) {
        registeredItems.put(item.getId().toLowerCase(), item);
    }

    public CustomItem getItemById(String id) {
        return registeredItems.get(id.toLowerCase());
    }

    public CustomItem getCustomItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;

        for (CustomItem customItem : registeredItems.values()) {
            if (customItem.isItem(itemStack)) {
                return customItem;
            }
        }
        return null;
    }

    // PATCHED: Tab completion support
    public Iterable<String> getRegisteredIds() { 
        return registeredItems.keySet(); 
    }
}