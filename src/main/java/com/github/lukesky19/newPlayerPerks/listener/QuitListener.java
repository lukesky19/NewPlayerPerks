package com.github.lukesky19.newPlayerPerks.listener;

import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
    private final PerksManager invulnerableManager;

    public QuitListener(PerksManager invulnerableManager) {
        this.invulnerableManager = invulnerableManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        invulnerableManager.handleLogout(event.getPlayer(), event.getPlayer().getUniqueId());
    }
}
