package com.kairos.api.entities;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.plugin.java.JavaPlugin;

import com.kairos.KairosPlugin;

public abstract class CustomEntity {

    protected LivingEntity entity;
    protected final CustomEntityType type;
    protected int ticksLived = 0;

    public CustomEntity(LivingEntity entity, CustomEntityType type) {
        this.entity = entity;
        this.type = type;

        // --- AUTOMATIC CONFIG STATS ---
        FileConfiguration config = JavaPlugin.getPlugin(KairosPlugin.class).getConfigManager().getConfig("mobs.yml");
        
        if (config.contains(type.id + ".movement_speed")) {
            AttributeInstance speed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(config.getDouble(type.id + ".movement_speed"));
        }
        
        if (config.contains(type.id + ".attack_damage")) {
            AttributeInstance damage = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            if (damage != null) damage.setBaseValue(config.getDouble(type.id + ".attack_damage"));
        }
    }

    // --- CONFIG HELPER METHODS FOR ABILITIES ---
    protected double getConfigDouble(String key, double fallback) {
        return JavaPlugin.getPlugin(KairosPlugin.class).getConfigManager().getConfig("mobs.yml")
                .getDouble(type.id + "." + key, fallback);
    }

    protected int getConfigInt(String key, int fallback) {
        return JavaPlugin.getPlugin(KairosPlugin.class).getConfigManager().getConfig("mobs.yml")
                .getInt(type.id + "." + key, fallback);
    }

    protected boolean getConfigBoolean(String key, boolean fallback) {
        return JavaPlugin.getPlugin(KairosPlugin.class).getConfigManager().getConfig("mobs.yml")
                .getBoolean(type.id + "." + key, fallback);
    }

    public void morph(LivingEntity newEntity) {
        KairosPlugin plugin = JavaPlugin.getPlugin(KairosPlugin.class);
        LivingEntity old = this.entity;

        double healthRatio = 1.0;
        AttributeInstance oldAttr = old.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (oldAttr != null && oldAttr.getValue() > 0) {
            healthRatio = old.getHealth() / oldAttr.getValue();
        }

        String customName = old.getCustomName();
        boolean customNameVisible = old.isCustomNameVisible();
        int fireTicks = old.getFireTicks();
        var potionEffects = old.getActivePotionEffects();

        EntityEquipment oldEquip = old.getEquipment();
        EntityEquipment newEquip = newEntity.getEquipment();

        plugin.getEntityRegistry().unloadEntity(old);

        NamespacedKey key = new NamespacedKey(plugin, "kairos_custom_entity");
        newEntity.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.type.id);

        AttributeInstance newAttr = newEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (newAttr != null && newAttr.getValue() > 0) {
            double newHealth = Math.max(1.0, newAttr.getValue() * healthRatio);
            newEntity.setHealth(Math.min(newHealth, newAttr.getValue()));
        }

        newEntity.setCustomName(customName);
        newEntity.setCustomNameVisible(customNameVisible);
        newEntity.setFireTicks(fireTicks);
        newEntity.setVelocity(old.getVelocity());

        for (PotionEffect effect : potionEffects) {
            newEntity.addPotionEffect(effect);
        }

        if (oldEquip != null && newEquip != null) {
            newEquip.setHelmet(oldEquip.getHelmet());
            newEquip.setChestplate(oldEquip.getChestplate());
            newEquip.setLeggings(oldEquip.getLeggings());
            newEquip.setBoots(oldEquip.getBoots());
            newEquip.setItemInMainHand(oldEquip.getItemInMainHand());
            newEquip.setItemInOffHand(oldEquip.getItemInOffHand());
        }

        old.remove();
        this.entity = newEntity;
        plugin.getEntityRegistry().registerEntity(this);
    }

    public LivingEntity getBukkitEntity() { return entity; }
    public String getId() { return type.id; }
    public CustomEntityType getType() { return type; }

    public void baseTick() {
        ticksLived++;
        tick();
    }

    public abstract void tick();
    public abstract void cleanUp();
}