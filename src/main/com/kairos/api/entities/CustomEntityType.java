package com.kairos.api.entities;

import com.kairos.KairosPlugin;
import com.kairos.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class CustomEntityType {

    private static final Map<String, CustomEntityType> REGISTRY = new HashMap<>();

    public final String id;
    public final String species;
    public final String colChar;
    public final double maxHealth;
    public final EntityType baseType;

    public CustomEntityType(String id, String species, String colChar, double maxHealth, EntityType baseType) {
        this.id = id.toLowerCase();
        this.species = species;
        this.colChar = colChar;
        this.maxHealth = maxHealth;
        this.baseType = baseType;
    }

    public static void register(CustomEntityType type) {
        REGISTRY.put(type.id, type);
    }

    public static CustomEntityType getCustomEntityType(String id) {
        return REGISTRY.get(id.toLowerCase());
    }

    public static Collection<CustomEntityType> values() {
        return REGISTRY.values();
    }

    public static List<String> speciesList = REGISTRY.values().stream().map(t -> t.id).collect(Collectors.toList());

    public CustomEntity spawn(Location location) {
        KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
        FileConfiguration config = plugin.getConfigManager().getConfig("mobs.yml");
        
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, baseType);
        
        // 1. Dynamic Health from mobs.yml
        double actualHealth = config.getDouble(this.id + ".max_health", this.maxHealth);
        AttributeInstance healthAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        
        if (healthAttr != null) {
            healthAttr.setBaseValue(actualHealth);
        }
        entity.setHealth(actualHealth);
        
        entity.setCustomNameVisible(true);
        entity.setCustomName(ColorUtils.colorize(colChar + species));
        
        // 2. Tag it in PersistentDataContainer
        NamespacedKey key = new NamespacedKey(plugin, "kairos_custom_entity");
        entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.id);
        
        // 3. Instantiate and register wrapper
        CustomEntity customEntity = instantiateWrapper(entity);
        plugin.getEntityRegistry().registerEntity(customEntity);
        
        return customEntity;
    }

    public CustomEntity rebind(LivingEntity entity) {
        CustomEntity customEntity = instantiateWrapper(entity);
        JavaPlugin.getPlugin(KairosPlugin.class).getEntityRegistry().registerEntity(customEntity);
        return customEntity;
    }

    public double getHealth() { return maxHealth; }
    public String getColChar() { return colChar; }

    protected abstract CustomEntity instantiateWrapper(LivingEntity entity);
}