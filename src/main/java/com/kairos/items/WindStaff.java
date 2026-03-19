package com.kairos.items;

import com.kairos.api.items.CustomItem;
import com.kairos.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class WindStaff extends CustomItem {

    public WindStaff() {
        super("wind_staff");
    }

    @Override
    public ItemStack buildItem() {
        String matName = getConfigString("material", "STICK");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.STICK;

        String name = getConfigString("name", "&f&lStaff of Winds");
        List<String> lore = getConfigStringList("lore");
        int customModelData = getConfigInt("custom_model_data", 0); // NEW!
        
        if (lore.isEmpty()) {
            lore = Arrays.asList("&7A relic from a Category 5 Hurricane.", "", "&eRight-Click &7to cast a blast of wind!");
        }

        ItemStack item = new ItemBuilder(mat)
                .name(name)
                .lore(lore.toArray(new String[0]))
                .hideAttributes()
                .customModelData(customModelData > 0 ? customModelData : null) // NEW!
                .build();

        ItemMeta meta = item.getItemMeta();
        applyTag(meta); 
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player p = event.getPlayer();
            
            if (p.hasCooldown(event.getMaterial())) return;
            
            int cooldown = getConfigInt("cooldown_ticks", 60);
            double pushForce = getConfigDouble("abilities.push_force", 1.5);
            double upwardLift = getConfigDouble("abilities.upward_lift", 0.5);
            double radius = getConfigDouble("abilities.radius", 5.0);

            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
            p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.2);

            Vector direction = p.getLocation().getDirection().normalize();
            for (Entity target : p.getNearbyEntities(radius, radius, radius)) {
                
                // INTEGRATION FIX: Create a fake damage event to test if PvP/Mob damage is allowed here!
                if (target instanceof LivingEntity) {
                    EntityDamageByEntityEvent damageCheck = new EntityDamageByEntityEvent(p, target, EntityDamageEvent.DamageCause.CUSTOM, 0.0);
                    Bukkit.getPluginManager().callEvent(damageCheck);
                    
                    if (damageCheck.isCancelled()) continue; // Skip them if PvP is disabled!
                }
                
                target.setVelocity(direction.multiply(pushForce).setY(upwardLift));
            }
            
            p.setCooldown(event.getMaterial(), cooldown); 
        }
    }
}