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
package com.github.lukesky19.newPlayerPerks.data;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the plugin's locale.
 * @param configVersion The locale's config version.
 * @param prefix The plugin's prefix.
 * @param reload The message sent when the plugin is reloaded.
 * @param addedPerks The message sent to the player who added perks to another player.
 * @param removedPerks The message sent to the player who removed perks from another player.
 * @param playerDataError The message sent when a request fails due to no player data found.
 * @param userError The message sent when a request fails due to no LuckPerms' User found.
 * @param settingsError The message sent when a request fails due to a settings error.
 * @param expiredError The message sent when a request fails due to perks being expired.
 * @param enablePerksExpired The message sent when a player tries to enable their perks that have already expired.
 * @param disablePerksExpired The message sent when a player tries to disable their perks that have already expired.
 * @param disablePerksReload The message sent when a player has their perks disabled due to a plugin reload.
 * @param perksAddedMessages The messages sent to the player who had perks applied to them.
 * @param perksEnabledMessages The messages sent to the player who enabled their perks.
 * @param perksRemovedMessages The messages sent to the player who had perks removed from them.
 * @param perksDisabledMessages The messages sent to the player who disabled their perks.
 * @param perksExpiredMessages The messages sent to the player who had perks expire.
 * @param timeMessage The {@link TimeMessage} config to produce a formatted timestamp message.
 */
@ConfigSerializable
public record Locale(
        @Nullable String configVersion,
        String prefix,
        String reload,
        String addedPerks,
        String removedPerks,
        String playerDataError,
        String userError,
        String settingsError,
        String expiredError,
        String enablePerksExpired,
        String disablePerksExpired,
        String disablePerksReload,
        @NotNull List<String> perksAddedMessages,
        @NotNull List<String> perksEnabledMessages,
        @NotNull List<String> perksRemovedMessages,
        @NotNull List<String> perksDisabledMessages,
        @NotNull List<String> perksExpiredMessages,
        @NotNull TimeMessage timeMessage) {
    /**
     * This record contains the configuration to create a formatted timestamp message.
     * @param prefix The text to display before the first time unit.
     * @param years The text to display when the player's time enters years.
     * @param months The text to display when the player's time enters months.
     * @param weeks The text to display when the player's time enters weeks.
     * @param days The text to display when the player's time enters days.
     * @param hours The text to display when the player's time enters hours.
     * @param minutes The text to display when the player's time enters minutes.
     * @param seconds The text to display when the player's time enters seconds.
     * @param suffix The text to display after the last time unit.
     */
    @ConfigSerializable
    public record TimeMessage(
            String prefix,
            String years,
            String months,
            String weeks,
            String days,
            String hours,
            String minutes,
            String seconds,
            String suffix) {}
}
