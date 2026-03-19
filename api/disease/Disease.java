package com.kairos.api.disease;

import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Set;

public class Disease {
    private final String id;
    private final String displayName;
    private final Set<EntityType> hosts;
    private final List<String> allowedSeasons; // NEW: Season integration
    
    private final boolean proximityEnabled;
    private final double proximityRadius;
    private final double proximityChance;
    
    private final boolean attackEnabled;
    private final double attackChance;
    
    private final List<PotionEffect> effects;
    private final double damagePerTick;
    
    private final String contractedMsg;
    private final String symptomMsg;
    private final String curedMsg;
    
    private final List<String> cures; 
    
    private final boolean outbreakEnabled;
    private final int outbreakCrowdThreshold;
    private final double outbreakChance;

    public Disease(String id, String displayName, Set<EntityType> hosts, List<String> allowedSeasons, boolean proximityEnabled, double proximityRadius, double proximityChance, boolean attackEnabled, double attackChance, List<PotionEffect> effects, double damagePerTick, String contractedMsg, String symptomMsg, String curedMsg, List<String> cures, boolean outbreakEnabled, int outbreakCrowdThreshold, double outbreakChance) {
        this.id = id;
        this.displayName = displayName;
        this.hosts = hosts;
        this.allowedSeasons = allowedSeasons;
        this.proximityEnabled = proximityEnabled;
        this.proximityRadius = proximityRadius;
        this.proximityChance = proximityChance;
        this.attackEnabled = attackEnabled;
        this.attackChance = attackChance;
        this.effects = effects;
        this.damagePerTick = damagePerTick;
        this.contractedMsg = contractedMsg;
        this.symptomMsg = symptomMsg;
        this.curedMsg = curedMsg;
        this.cures = cures;
        this.outbreakEnabled = outbreakEnabled;
        this.outbreakCrowdThreshold = outbreakCrowdThreshold;
        this.outbreakChance = outbreakChance;
    }

    // NEW: Season Check
    public boolean canContractInSeason(String seasonName) {
        return allowedSeasons == null || allowedSeasons.isEmpty() || allowedSeasons.contains(seasonName.toUpperCase());
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Set<EntityType> getHosts() { return hosts; }
    
    public boolean isProximityEnabled() { return proximityEnabled; }
    public double getProximityRadius() { return proximityRadius; }
    public double getProximityChance() { return proximityChance; }
    
    public boolean isAttackEnabled() { return attackEnabled; }
    public double getAttackChance() { return attackChance; }
    
    public List<PotionEffect> getEffects() { return effects; }
    public double getDamagePerTick() { return damagePerTick; }
    
    public String getContractedMsg() { return contractedMsg; }
    public String getSymptomMsg() { return symptomMsg; }
    public String getCuredMsg() { return curedMsg; }
    
    public List<String> getCures() { return cures; }
    
    public boolean isOutbreakEnabled() { return outbreakEnabled; }
    public int getOutbreakCrowdThreshold() { return outbreakCrowdThreshold; }
    public double getOutbreakChance() { return outbreakChance; }
}