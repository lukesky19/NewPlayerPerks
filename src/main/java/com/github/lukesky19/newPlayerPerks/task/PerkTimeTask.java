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
import com.github.lukesky19.newPlayerPerks.locale.Locale;
import com.github.lukesky19.newPlayerPerks.locale.LocaleManager;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.PlayerDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Loops through players with active perks and decrements time.
 */
public class PerkTimeTask extends BukkitRunnable {
    private final @NotNull Server server;
    private final @NotNull Locale locale;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull PerksManager perksManager;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param perksManager A {@link PerksManager} instance.
     */
    public PerkTimeTask(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull LocaleManager localeManager,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull PerksManager perksManager) {
        this.server = newPlayerPerks.getServer();
        this.locale = localeManager.getLocale();
        this.playerDataManager = playerDataManager;
        this.perksManager = perksManager;
    }

    /**
     * Decrement perk time for all players with active perks and remove perks if required.
     */
    @Override
    public void run() {
        playerDataManager.getActivePerksPlayerData().forEach(playerData -> {
            UUID playerId = playerData.getPlayerId();
            @Nullable Player player = server.getPlayer(playerId);
            if(player != null && player.isOnline() && player.isConnected()) {
                playerData.removePerkTime(1);

                if(playerData.getPerkTime() <= 0) {
                    perksManager.disablePerks(player, playerId, false);

                    for(String msg : locale.perksExpiredMessages()) {
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + msg));
                    }
                }
            }
        });
    }
}