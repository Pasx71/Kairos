package com.kairos.core;

import com.kairos.api.entities.CustomEntity;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

import com.kairos.api.entities.CustomEntityType;

public class CustomEntityRegistry {

    private final Plugin plugin;
    private final Map<UUID, CustomEntity> activeCustomEntities = new ConcurrentHashMap<>();
    
    public CustomEntityRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerEntity(CustomEntity customEntity) {
        if (customEntity.getBukkitEntity() != null) {
            activeCustomEntities.put(customEntity.getBukkitEntity().getUniqueId(), customEntity);
        }
    }

   public void rebindEntity(String entityTypeId, Entity entity) {
        if (!(entity instanceof org.bukkit.entity.LivingEntity)) return;
        
        CustomEntityType type = CustomEntityType.getCustomEntityType(entityTypeId);
        if (type != null) {
            type.rebind((org.bukkit.entity.LivingEntity) entity);
        }
    }

    public void unloadEntity(Entity entity) {
        CustomEntity wrapper = activeCustomEntities.remove(entity.getUniqueId());
        if (wrapper != null) wrapper.cleanUp();
    }

    public CustomEntity getCustomEntity(Entity entity) {
        return activeCustomEntities.get(entity.getUniqueId());
    }
    
    // GAP PATCHED: Added to support /disasters entities command
    public Collection<CustomEntity> getActiveEntities() {
        return activeCustomEntities.values();
    }
}