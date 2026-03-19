package com.kairos.listeners;

import com.kairos.KairosPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.persistence.PersistentDataType;

public class EntityPersistenceListener implements Listener {
    
    private final KairosPlugin plugin;
    private final NamespacedKey customEntityKey;

    public EntityPersistenceListener(KairosPlugin plugin) {
        this.plugin = plugin;
        this.customEntityKey = new NamespacedKey(plugin, "kairos_custom_entity");
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent e) {
        for (Entity entity : e.getEntities()) {
            if (entity.isValid() && entity.getPersistentDataContainer().has(customEntityKey, PersistentDataType.STRING)) {
                String entityTypeId = entity.getPersistentDataContainer().get(customEntityKey, PersistentDataType.STRING);
                
                // GAP PATCHED: Actually calls the CustomEntityRegistry now!
                plugin.getEntityRegistry().rebindEntity(entityTypeId, entity);
            }
        }
    }

    @EventHandler
    public void onEntitiesUnload(EntitiesUnloadEvent e) {
        for (Entity entity : e.getEntities()) {
            if (entity.getPersistentDataContainer().has(customEntityKey, PersistentDataType.STRING)) {
                
                // GAP PATCHED: Safely detaches the entity to prevent memory leaks!
                plugin.getEntityRegistry().unloadEntity(entity);
            }
        }
    }
}