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

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.PlayerDataManager;
import com.github.lukesky19.newPlayerPerks.manager.SettingsManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class listens to when a player disconnects from the server and removes any applied perks.
 */
public class QuitListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull PerksManager perksManager;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param perksManager A {@link PerksManager} instance.
     */
    public QuitListener(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull SettingsManager settingsManager,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull PerksManager perksManager) {
        this.logger = newPlayerPerks.getComponentLogger();
        this.settingsManager = settingsManager;
        this.playerDataManager = playerDataManager;
        this.perksManager = perksManager;
    }

    /**
     * Listens for a {@link PlayerQuitEvent} for when a player disconnects from the server and removes any applied perks.
     * @param playerQuitEvent A {@link PlayerQuitEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        if(playerData == null) return;
        if(settingsManager.getPeriod() == null) {
            logger.error(AdventureUtil.serialize("Unable to check if perks should be removed due to an invalid period in settings.yml."));
            return;
        }

        if(System.currentTimeMillis() < (playerData.joinTime() + settingsManager.getPeriod())) {
            perksManager.removePerks(playerQuitEvent.getPlayer(), playerQuitEvent.getPlayer().getUniqueId(), false);
        }
    }
}
