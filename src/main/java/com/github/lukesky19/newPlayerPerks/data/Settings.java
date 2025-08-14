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
import org.jetbrains.annotations.Nullable;

/**
 * This record contains the plugin's settings.
 * @param locale The locale configuration to use.
 * @param invulnerable Should new players be invulnerable?
 * @param fly Should new players have access to /fly? (Essentials and BentoBox's IslandFly addon)
 * @param keepInventory Should new players have access to keep inventory?
 * @param keepExp Should new players have access to keep exp?
 * @param voidTeleport Should new players be teleported to their island when they fall off?
 * @param period The period that new perks last for.
 */
@ConfigSerializable
public record Settings(
        @Nullable String locale,
        boolean invulnerable,
        boolean fly,
        boolean keepInventory,
        boolean keepExp,
        boolean voidTeleport,
        @Nullable String period) {}
