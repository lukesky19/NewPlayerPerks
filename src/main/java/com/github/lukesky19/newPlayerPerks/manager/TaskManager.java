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
package com.github.lukesky19.newPlayerPerks.manager;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.newPlayerPerks.manager.config.SettingsManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * This class manages the task that checks if perks need to be disabled for a player.
 */
public class TaskManager {
    private final @NotNull NewPlayerPerks newPlayerPerks;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull PerksManager perksManager;
    private @Nullable BukkitTask checkPerksTask;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param perksManager A {@link PerksManager} instance.
     */
    public TaskManager(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull SettingsManager settingsManager,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull PerksManager perksManager) {
        this.newPlayerPerks = newPlayerPerks;
        this.logger = newPlayerPerks.getComponentLogger();
        this.settingsManager = settingsManager;
        this.playerDataManager = playerDataManager;
        this.perksManager = perksManager;
    }

    /**
     * Start the {@link BukkitTask} that checks perks.
     */
    public void startCheckPerksTask() {
        checkPerksTask = newPlayerPerks.getServer().getScheduler().runTaskTimer(newPlayerPerks, this::checkPerks, 20L, 20L);
    }

    /**
     * Checks whether players with perks need them removed or not.
     */
    private void checkPerks() {
        if(settingsManager.getPeriod() == null) {
            logger.error(AdventureUtil.serialize("Unable to check perks should be removed due to an invalid period in settings.yml."));
            return;
        }

        Iterator<Map.Entry<UUID, PlayerData>> iterator = playerDataManager.getActivePerksMap().entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<UUID, PlayerData> entry = iterator.next();
            UUID uuid = entry.getKey();
            Player player = newPlayerPerks.getServer().getPlayer(uuid);
            if(player == null || !player.isOnline() || !player.isConnected()) continue;
            PlayerData playerData = entry.getValue();

            if(System.currentTimeMillis() > (playerData.getJoinTime() + settingsManager.getPeriod())) {
                iterator.remove();

                playerDataManager.removeFromActivePerksMap(uuid);
                perksManager.disablePerks(player, uuid, false);
            }
        }
    }

    /**
     * Stop the {@link BukkitTask} that checks perks.
     */
    public void stopCheckPerksTask() {
        if(checkPerksTask == null) return;
        if(checkPerksTask.isCancelled()) {
            checkPerksTask = null;
            return;
        }

        checkPerksTask.cancel();
        checkPerksTask = null;
    }
}
