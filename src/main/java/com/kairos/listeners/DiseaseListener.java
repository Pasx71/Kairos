package com.kairos.listeners;

import com.kairos.KairosPlugin;
import com.kairos.api.disease.Disease;
import com.kairos.api.items.CustomItem;
import com.kairos.core.DiseaseManager;
import com.kairos.utils.ColorUtils;
import com.kairos.utils.PlaceholderUtils;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DiseaseListener implements Listener {

    private final KairosPlugin plugin;
    private final DiseaseManager manager;

    public DiseaseListener(KairosPlugin plugin, DiseaseManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!manager.isEnabled()) return;
        
        if (event.getDamager() instanceof LivingEntity attacker && event.getEntity() instanceof LivingEntity victim) {
            for (Disease disease : manager.getActiveDiseases(attacker)) {
                if (disease.isAttackEnabled()) {
                    
                    double finalChance = disease.getAttackChance();

                    if (victim instanceof Player p) {
                        FileConfiguration config = plugin.getConfigManager().getConfig("diseases.yml");
                        String placeholder = config.getString("integration.resistance.placeholder", "none");
                        double scale = config.getDouble("integration.resistance.scale", 0.0);
                        
                        double resistance = PlaceholderUtils.parseDouble(p, placeholder, 0.0);
                        finalChance -= (resistance * scale);
                    }

                    if (finalChance > 0 && Math.random() < finalChance) {
                        if (disease.getHosts().contains(victim.getType()) && !manager.hasDisease(victim, disease)) {
                            
                            String currentSeason = plugin.getSeasonManager().getCurrentSeason(victim.getWorld()).name();
                            if (disease.canContractInSeason(currentSeason)) {
                                manager.infect(victim, disease);
                                if (victim instanceof Player p) {
                                    p.sendMessage(ColorUtils.colorize(disease.getContractedMsg()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!manager.isEnabled()) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        String itemIdentifier = item.getType().name();
        CustomItem customItem = plugin.getItemRegistry().getCustomItem(item);
        if (customItem != null) {
            itemIdentifier = customItem.getId();
        }

        List<Disease> activeDiseases = manager.getActiveDiseases(player);
        for (Disease disease : activeDiseases) {
            if (disease.getCures().contains(itemIdentifier)) {
                manager.cure(player, disease);
                player.sendMessage(ColorUtils.colorize(disease.getCuredMsg()));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
            }
        }
    }
}