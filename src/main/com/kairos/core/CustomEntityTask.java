package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.api.entities.CustomEntity;

import java.util.Iterator;

public class CustomEntityTask implements Runnable {

    private final KairosPlugin plugin;

    public CustomEntityTask(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // We use an iterator so we can safely remove dead entities from the list without throwing a ConcurrentModificationException
        Iterator<CustomEntity> iterator = plugin.getEntityRegistry().getActiveEntities().iterator();
        
        while (iterator.hasNext()) {
            CustomEntity customEntity = iterator.next();
            
            // If the entity died, fell in the void, or was deleted by another plugin:
            if (customEntity.getBukkitEntity() == null || !customEntity.getBukkitEntity().isValid() || customEntity.getBukkitEntity().isDead()) {
                customEntity.cleanUp();
                iterator.remove(); // Safely remove from active memory
                continue;
            }
            
            // Execute the custom AI
            customEntity.baseTick();
        }
    }
}