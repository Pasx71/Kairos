package com.kairos.listeners;

import com.kairos.KairosPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final KairosPlugin plugin;

    public PlayerConnectionListener(KairosPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Save their disaster timer to disk when they log off
        plugin.getPlayerDataManager().savePlayer(e.getPlayer().getUniqueId());
        plugin.getPlayerDataManager().saveAll(); // Force file write
    }
}