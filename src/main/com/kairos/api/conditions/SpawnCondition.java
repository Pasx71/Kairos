package com.kairos.api.conditions;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface SpawnCondition {
    boolean test(Player player);
}