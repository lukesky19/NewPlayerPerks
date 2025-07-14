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
import com.github.lukesky19.newPlayerPerks.configuration.locale.LocaleManager;
import com.github.lukesky19.newPlayerPerks.configuration.settings.SettingsManager;
import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.PlayerDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class listens to when a player joins. It creates and loads player data as needed and applies perks if required.
 */
public class JoinListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull PerksManager perksManager;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param perksManager A {@link PerksManager} instance.
     */
    public JoinListener(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull PerksManager perksManager) {
        this.logger = newPlayerPerks.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.playerDataManager = playerDataManager;
        this.perksManager = perksManager;
    }

    /**
     * Listens for a {@link PlayerJoinEvent} and creates or loads player data as needed.
     * Perks are applied if the system time is less than the player's join time plus the period perks are applied for.
     * @param playerJoinEvent A {@link PlayerJoinEvent}.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        CompletableFuture<PlayerData> future = playerDataManager.loadPlayerData(uuid);

        future.whenComplete((playerData, throwable) -> {
            if(throwable != null) {
                logger.error(AdventureUtil.serialize("Loading of player data failed: " + throwable.getMessage()));
                return;
            }

            if(settingsManager.getPeriod() == null) {
                logger.error(AdventureUtil.serialize("Unable to check if perks should be applied due to an invalid period in settings.yml."));
                return;
            }

            System.out.println(System.currentTimeMillis());
            System.out.println((playerData.joinTime() + settingsManager.getPeriod()));

            if(System.currentTimeMillis() < (playerData.joinTime() + settingsManager.getPeriod())) {
                perksManager.applyPerks(player, uuid);
            }
        });

        if(!player.hasPlayedBefore()) {
            for(String msg : localeManager.getLocale().newPlayerMessages()) {
                player.sendMessage(AdventureUtil.serialize(player, localeManager.getLocale().prefix() + msg));
            }
        }
    }
}
