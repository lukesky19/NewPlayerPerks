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

import java.util.List;

/**
 * This record contains the plugin's locale.
 * @param prefix The plugin's prefix.
 * @param reload The message sent when the plugin is reloaded.
 * @param addedPerks The message sent to the player who added perks to another player.
 * @param removedPerks The message sent to the player who removed perks from another player.
 * @param newPlayerMessages The {@link List} of {@link String}s to send to the player who is new.
 * @param perksExpireMessages The {@link List} of {@link String}s to send when a player's perks expire.
 */
@ConfigSerializable
public record Locale(
        String prefix,
        String reload,
        String addedPerks,
        String removedPerks,
        List<String> newPlayerMessages,
        List<String> perksExpireMessages) {}
