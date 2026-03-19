package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.api.items.CustomItem;
import com.kairos.utils.PlaceholderUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class LootManager implements Listener {

    private final KairosPlugin plugin;
    private final NamespacedKey customEntityKey;
    private final Random random = new Random();

    public LootManager(KairosPlugin plugin) {
        this.plugin = plugin;
        this.customEntityKey = new NamespacedKey(plugin, "kairos_custom_entity");
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCustomMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!entity.getPersistentDataContainer().has(customEntityKey, PersistentDataType.STRING)) {
            return;
        }

        String mobId = entity.getPersistentDataContainer().get(customEntityKey, PersistentDataType.STRING);
        FileConfiguration lootConfig = plugin.getConfigManager().getConfig("loot.yml");

        if (!lootConfig.contains(mobId)) {
            return; 
        }

        String mode = lootConfig.getString(mobId + ".mode", "REPLACE").toUpperCase();

        if (mode.equals("REPLACE") || lootConfig.getBoolean(mobId + ".clear_vanilla_drops", false)) {
            event.getDrops().clear();
            event.setDroppedExp(lootConfig.getInt(mobId + ".dropped_exp", event.getDroppedExp()));
        }

        ConfigurationSection dropsSection = lootConfig.getConfigurationSection(mobId + ".drops");
        if (dropsSection == null) return;

        Player killer = entity.getKiller();

        for (String itemKey : dropsSection.getKeys(false)) {
            double baseChance = dropsSection.getDouble(itemKey + ".chance", 1.0);
            
            // Dynamic integration with skills/stats
            double finalChance = baseChance;
            if (killer != null) {
                String placeholder = dropsSection.getString(itemKey + ".modifier.placeholder", "none");
                double scale = dropsSection.getDouble(itemKey + ".modifier.scale", 0.0);
                double modifierValue = PlaceholderUtils.parseDouble(killer, placeholder, 0.0);
                finalChance += (modifierValue * scale);
            }

            if (finalChance > 0 && random.nextDouble() <= finalChance) {
                int min = dropsSection.getInt(itemKey + ".min", 1);
                int max = dropsSection.getInt(itemKey + ".max", 1);
                int amount = min >= max ? min : min + random.nextInt((max - min) + 1);

                ItemStack drop = getResolvedItem(itemKey, amount);
                if (drop != null) {
                    event.getDrops().add(drop);
                }
            }
        }
    }

    private ItemStack getResolvedItem(String key, int amount) {
        CustomItem customItem = plugin.getItemRegistry().getItemById(key);
        if (customItem != null) {
            ItemStack item = customItem.buildItem();
            item.setAmount(amount);
            return item;
        }

        Material material = Material.matchMaterial(key.toUpperCase());
        if (material != null) {
            return new ItemStack(material, amount);
        }

        plugin.getLogger().warning("Could not find valid item or material for loot key: " + key);
        return null;
    }
}