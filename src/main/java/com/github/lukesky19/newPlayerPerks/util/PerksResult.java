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
package com.github.lukesky19.newPlayerPerks.util;

/**
 * This enum is used to identify the result of adding, enabling, removing, and disabling perks.
 */
public enum PerksResult {
    /**
     * When perks were successfully added or removed.
     */
    SUCCESS,
    /**
     * When perks can't be enabled because they expired.
     */
    EXPIRED,
    /**
     * When perks can't be added, enabled, or removed because the plugin's settings are invalid.
     */
    SETTINGS_ERROR,
    /**
     * When perks can't be added, enabled, or removed because the player has no player data.
     */
    NO_PLAYER_DATA,
    /**
     * When perks can't be added, enabled, or removed because the LuckPerms' user can't be found for the player.
     */
    USER_ERROR
}
