package com.kairos.listeners;

import com.kairos.KairosPlugin;
import com.kairos.api.DisasterType;
import com.kairos.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class DisasterDeathListener implements Listener {

    private final KairosPlugin plugin;
    private final NamespacedKey deathReasonKey;
    private final NamespacedKey purgeMobKey;

    public DisasterDeathListener(KairosPlugin plugin) {
        this.plugin = plugin;
        this.deathReasonKey = new NamespacedKey(plugin, "recent_disaster_damage");
        this.purgeMobKey = new NamespacedKey(plugin, "kairos_purge_mob");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        
        if (player.getPersistentDataContainer().has(deathReasonKey, PersistentDataType.STRING)) {
            String disasterId = player.getPersistentDataContainer().get(deathReasonKey, PersistentDataType.STRING);
            DisasterType type = DisasterType.forName(disasterId);
            
            if (type != null) {
                String messagePath = "messages.deaths." + type.getId();
                String defaultMsg = player.getName() + " was killed by a " + type.getDisplayName() + "!";
                
                String customMessage = plugin.getConfig().getString(messagePath, defaultMsg);
                e.setDeathMessage(ColorUtils.colorize(customMessage.replace("%player%", player.getName())));
                
                player.getPersistentDataContainer().remove(deathReasonKey);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof WitherSkeleton) {
            if (e.getEntity().getPersistentDataContainer().has(purgeMobKey, PersistentDataType.BYTE)) {
                
                // INTEGRATION FIX: Safely remove ONLY vanilla skulls without wiping custom plugin items
                boolean hadVanillaSkull = e.getDrops().removeIf(item -> item != null && item.getType() == Material.WITHER_SKELETON_SKULL && !item.hasItemMeta());
                
                if (hadVanillaSkull || Math.random() < 0.000025) {
                    if (Math.random() < 0.000025) {
                        e.getDrops().add(new ItemStack(Material.WITHER_SKELETON_SKULL));
                    }
                }
            }
        }
    }
}