package com.kairos.core;

import com.kairos.KairosPlugin;
import com.kairos.api.disease.Disease;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class DiseaseManager {

    private final KairosPlugin plugin;
    private final Map<String, Disease> diseases = new HashMap<>();
    private final NamespacedKey infectedKey;
    private final boolean enabled;

    public DiseaseManager(KairosPlugin plugin) {
        this.plugin = plugin;
        this.infectedKey = new NamespacedKey(plugin, "kairos_diseases");
        
        FileConfiguration config = plugin.getConfigManager().getConfig("diseases.yml");
        this.enabled = config.getBoolean("enabled", true);
        
        if (this.enabled) loadDiseases(config);
    }

    public boolean isEnabled() { return enabled; }
    public Collection<Disease> getAllDiseases() { return diseases.values(); }
    public Disease getDisease(String id) { return diseases.get(id); }

    private void loadDiseases(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("diseases");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String path = key + ".";
            // Inside the for-loop of loadDiseases():
        if (!section.getBoolean(path + "enabled", true)) continue; // Allows disabling individual diseases
            List<String> allowedSeasons = section.getStringList(path + "allowed_seasons");
            Set<EntityType> hosts = new HashSet<>();
            for (String h : section.getStringList(path + "hosts")) {
                try { hosts.add(EntityType.valueOf(h.toUpperCase())); } catch (Exception ignored) {}
            }

            List<PotionEffect> effects = new ArrayList<>();
            for (String eff : section.getStringList(path + "effects.potions")) {
                String[] parts = eff.split(":");
                if (parts.length == 3) {
                    PotionEffectType type = PotionEffectType.getByName(parts[0]);
                    if (type != null) {
                        effects.add(new PotionEffect(type, Integer.parseInt(parts[2]), Integer.parseInt(parts[1])));
                    }
                }
            }

            Disease disease = new Disease(
                key,
                section.getString(path + "display_name", key),
                hosts,
                allowedSeasons,
                section.getBoolean(path + "spread.proximity.enabled", false),
                section.getDouble(path + "spread.proximity.radius", 4.0),
                section.getDouble(path + "spread.proximity.chance", 0.05),
                section.getBoolean(path + "spread.attack.enabled", false),
                section.getDouble(path + "spread.attack.chance", 0.1),
                effects,
                section.getDouble(path + "effects.damage_tick", 0.0),
                section.getString(path + "messages.contracted", "&cYou caught a disease!"),
                section.getString(path + "messages.symptom", "&eYou feel sick..."),
                section.getString(path + "messages.cured", "&aYou have been cured!"),
                section.getStringList(path + "cures"),
                section.getBoolean(path + "outbreak.enabled", false),
                section.getInt(path + "outbreak.crowd_threshold", 8),
                section.getDouble(path + "outbreak.chance", 0.05)
            );
            diseases.put(key, disease);
        }
    }

    // --- ENTITY PDC API ---

    public List<Disease> getActiveDiseases(LivingEntity entity) {
        List<Disease> active = new ArrayList<>();
        if (!entity.getPersistentDataContainer().has(infectedKey, PersistentDataType.STRING)) return active;
        
        String data = entity.getPersistentDataContainer().get(infectedKey, PersistentDataType.STRING);
        if (data == null || data.isEmpty()) return active;

        for (String id : data.split(",")) {
            Disease d = getDisease(id);
            if (d != null) active.add(d);
        }
        return active;
    }

    public boolean hasDisease(LivingEntity entity, Disease disease) {
        return getActiveDiseases(entity).contains(disease);
    }

    public void infect(LivingEntity entity, Disease disease) {
        if (!disease.getHosts().contains(entity.getType())) return; // Immune!
        if (hasDisease(entity, disease)) return;

        // API INTEGRATION: Fire the event and check if it was cancelled
        com.kairos.api.events.EntityContractDiseaseEvent event = 
                new com.kairos.api.events.EntityContractDiseaseEvent(entity, disease);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) return;

        List<Disease> current = getActiveDiseases(entity);
        current.add(event.getDisease()); // Use the disease from the event in case a plugin changed it!
        saveDiseases(entity, current);
    }

    public void cure(LivingEntity entity, Disease disease) {
        List<Disease> current = getActiveDiseases(entity);
        if (current.remove(disease)) {
            saveDiseases(entity, current);
        }
    }

    private void saveDiseases(LivingEntity entity, List<Disease> current) {
        if (current.isEmpty()) {
            entity.getPersistentDataContainer().remove(infectedKey);
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < current.size(); i++) {
            builder.append(current.get(i).getId());
            if (i < current.size() - 1) builder.append(",");
        }
        entity.getPersistentDataContainer().set(infectedKey, PersistentDataType.STRING, builder.toString());
    }
}