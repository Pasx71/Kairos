package com.kairos.api.events;

import com.kairos.api.disease.Disease;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EntityContractDiseaseEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity entity;
    private Disease disease;
    private boolean cancelled;

    public EntityContractDiseaseEvent(LivingEntity entity, Disease disease) {
        this.entity = entity;
        this.disease = disease;
    }

    public LivingEntity getEntity() { return entity; }
    public Disease getDisease() { return disease; }
    
    // Allows another plugin to swap out which disease they get!
    public void setDisease(Disease disease) { this.disease = disease; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}