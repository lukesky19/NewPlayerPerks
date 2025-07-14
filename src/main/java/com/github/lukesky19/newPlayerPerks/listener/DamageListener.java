/*
    NewPlayerPerks applies specific perks to new players.
    Copyright (C) 2024 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.newPlayerPerks.listener;

import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This class listens to when a player attempts to damage another player. If they have new player perks and is invulnerable, the damage is cancelled.
 */
public class DamageListener implements Listener {
    private final @NotNull PerksManager perksManager;

    /**
     * Constructor
     * @param perksManager A {@link PerksManager} instance.
     */
    public DamageListener(@NotNull PerksManager perksManager) {
        this.perksManager = perksManager;
    }

    /**
     * Listens for a {@link EntityDamageEvent} when a player attempts to damage another player. If they have new player perks and is invulnerable, the damage is cancelled.
     * @param entityDamageEvent An {@link EntityDamageEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent entityDamageEvent) {
        if(entityDamageEvent.getEntity() instanceof Player && entityDamageEvent.getDamageSource().getDirectEntity() instanceof Player source) {
            if(perksManager.doesPlayerHavePerks(source) && source.isInvulnerable()) {
                entityDamageEvent.setCancelled(true);
            }
        }
    }
}
