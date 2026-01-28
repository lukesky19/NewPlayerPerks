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
package com.github.lukesky19.newPlayerPerks.task;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.locale.LocaleManager;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.PlayerDataManager;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages the task that checks if perks need to be disabled for a player.
 */
public class TaskManager {
    private final @NotNull NewPlayerPerks newPlayerPerks;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull PerksManager perksManager;
    private @Nullable BukkitTask perkTimeTask;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param perksManager A {@link PerksManager} instance.
     */
    public TaskManager(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull LocaleManager localeManager,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull PerksManager perksManager) {
        this.newPlayerPerks = newPlayerPerks;
        this.localeManager = localeManager;
        this.playerDataManager = playerDataManager;
        this.perksManager = perksManager;
    }

    /**
     * Start the {@link BukkitTask} that decrements perk time and disables perks.
     */
    public void startPerkTimeTask() {
        perkTimeTask = new PerkTimeTask(newPlayerPerks, localeManager, playerDataManager, perksManager).runTaskTimer(newPlayerPerks, 20L, 20L);
    }

    /**
     * Stop the {@link BukkitTask} that decrements perk time and disables perks.
     */
    public void stopPerkTimeTask() {
        if(perkTimeTask == null) return;

        if(!perkTimeTask.isCancelled()) {
            perkTimeTask.cancel();
        }

        perkTimeTask = null;
    }
}