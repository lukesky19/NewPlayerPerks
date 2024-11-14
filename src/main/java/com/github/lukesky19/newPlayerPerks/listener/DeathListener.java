package com.github.lukesky19.newPlayerPerks.listener;

import com.github.lukesky19.newPlayerPerks.configuration.settings.Settings;
import com.github.lukesky19.newPlayerPerks.configuration.settings.SettingsManager;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
    private final PerksManager perksManager;
    private final SettingsManager settingsManager;

    public DeathListener(PerksManager perksManager, SettingsManager settingsManager) {
        this.perksManager = perksManager;
        this.settingsManager = settingsManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Settings settings = settingsManager.getSettings();

        if(perksManager.isPlayerNew(player)) {
            if(settings.keepInventory()) {
                event.setKeepInventory(true);
                event.getDrops().clear();
            }

            if(settings.keepExp()) {
                event.setKeepLevel(true);
                event.setDroppedExp(0);
            }
        }
    }
}
