package com.github.lukesky19.newPlayerPerks.listener;

import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

@SuppressWarnings("UnstableApiUsage")
public class DamageListener implements Listener {
    private final PerksManager perksManager;

    public DamageListener(PerksManager perksManager) {
        this.perksManager = perksManager;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player && event.getDamageSource().getDirectEntity() instanceof Player source) {
            if(perksManager.isPlayerNew(source) && source.isInvulnerable()) {
                event.setCancelled(true);
            }
        }
    }
}
