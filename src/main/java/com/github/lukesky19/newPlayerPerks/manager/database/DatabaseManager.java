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
package com.github.lukesky19.newPlayerPerks.manager.database;

import com.github.lukesky19.newPlayerPerks.manager.database.tables.PlayerDataTable;
import com.github.lukesky19.skylib.api.database.AbstractDatabaseManager;
import org.jetbrains.annotations.NotNull;

/**
 * This class manages access to the database table classes.
 */
public class DatabaseManager extends AbstractDatabaseManager {
    private final @NotNull PlayerDataTable playerDataTable;

    /**
     * Constructor
     * @param connectionManager Α {@link ConnectionManager} instance.
     * @param queueManager A {@link QueueManager} instance.
     */
    public DatabaseManager(@NotNull ConnectionManager connectionManager, @NotNull QueueManager queueManager) {
        super(connectionManager, queueManager);

        playerDataTable = new PlayerDataTable(queueManager);
        playerDataTable.createTable();
    }

    /**
     * Get the {@link PlayerDataTable}.
     * @return The {@link PlayerDataTable}.
     */
    public @NotNull PlayerDataTable getPlayerDataTable() {
        return playerDataTable;
    }
}