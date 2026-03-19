package com.kairos.api.items;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import com.kairos.KairosPlugin;

import java.util.List;

public abstract class CustomItem {
    
    private final String id;
    private final NamespacedKey key;

    public CustomItem(String id) {
        this.id = id;
        this.key = new NamespacedKey(JavaPlugin.getPlugin(KairosPlugin.class), "kairos_item_id");
    }

    public String getId() { return id; }

    // --- CONFIG HELPER METHODS ---
    protected FileConfiguration getConfig() {
        return JavaPlugin.getPlugin(KairosPlugin.class).getConfigManager().getConfig("items.yml");
    }

    protected String getConfigString(String key, String fallback) {
        return getConfig().getString(id + "." + key, fallback);
    }

    protected List<String> getConfigStringList(String key) {
        return getConfig().getStringList(id + "." + key);
    }

    protected double getConfigDouble(String key, double fallback) {
        return getConfig().getDouble(id + "." + key, fallback);
    }

    protected int getConfigInt(String key, int fallback) {
        return getConfig().getInt(id + "." + key, fallback);
    }

    // --- CORE LOGIC ---
    public abstract ItemStack buildItem();

    public boolean isItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            String itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            return this.id.equals(itemId);
        }
        return false;
    }

    protected void applyTag(ItemMeta meta) {
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.id);
    }

    public void onInteract(PlayerInteractEvent event) {}
    public void onAttack(EntityDamageByEntityEvent event, Player attacker) {}
}