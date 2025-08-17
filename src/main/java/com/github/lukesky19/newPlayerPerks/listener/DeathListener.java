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

import com.github.lukesky19.newPlayerPerks.data.Settings;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.config.SettingsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class listens to when a player dies. If they have new player perks, and keep inventory and or keep exp is configured, the player's inventory and or exp is set to not drop.
 */
public class DeathListener implements Listener {
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull PerksManager perksManager;

    /**
     * Constructor
     * @param settingsManager A {@link SettingsManager} instance.
     * @param perksManager A {@link PerksManager} instance.
     */
    public DeathListener(
            @NotNull SettingsManager settingsManager,
        @NotNull PerksManager perksManager) {
        this.settingsManager = settingsManager;
        this.perksManager = perksManager;
    }

    /**
     * Listens for a {@link PlayerDeathEvent}. If they have new player perks, and keep inventory and or keep exp is configured, the player's inventory and or exp is set to not drop.
     * @param playerDeathEvent A {@link PlayerDeathEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent playerDeathEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;
        Player player = playerDeathEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(perksManager.doesPlayerHavePerks(uuid)) {
            if(settings.keepInventory()) {
                playerDeathEvent.setKeepInventory(true);
                playerDeathEvent.getDrops().clear();
            }

            if(settings.keepExp()) {
                playerDeathEvent.setKeepLevel(true);
                playerDeathEvent.setDroppedExp(0);
            }
        }
    }
}
