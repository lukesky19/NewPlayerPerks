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
package com.github.lukesky19.newPlayerPerks.manager.database.tables;

import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.newPlayerPerks.manager.database.QueueManager;
import com.github.lukesky19.skylib.api.database.parameter.impl.LongParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.UUIDParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages access to the player data table in the database.
 */
public class PlayerDataTable {
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "newplayerperks_player_data";

    /**
     * Default Constructor.
     * You should use {@link #PlayerDataTable(QueueManager)} instead.
     * @deprecated You should use {@link #PlayerDataTable(QueueManager)} instead.
     */
    @Deprecated
    public PlayerDataTable() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param queueManager A {@link QueueManager} instance.
     */
    public PlayerDataTable(@NotNull QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    /**
     * Creates the table in the database if it doesn't exist and any indexes that don't exist.
     */
    public void createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "player_id LONG NOT NULL UNIQUE DEFAULT 0, " +
                "join_time LONG NOT NULL DEFAULT 0, " +
                "last_updated LONG NOT NULL DEFAULT 0)";
        String playerIdsIndexSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_player_ids ON " + tableName + "(player_id)";

        queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, playerIdsIndexSql));
    }


    /**
     * Loads the {@link PlayerData} for the {@link UUID} provided.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} containing {@link PlayerData}. May be null.
     */
    public @NotNull CompletableFuture<@Nullable PlayerData> loadPlayerData(@NotNull UUID uuid) {
        String selectSql = "SELECT join_time FROM " + tableName + " WHERE player_id = ? AND last_updated < ?";
        UUIDParameter uuidParameter = new UUIDParameter(uuid);
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        return queueManager.queueReadTransaction(selectSql, List.of(uuidParameter, lastUpdatedParameter), resultSet -> {
            try {
                if(!resultSet.next()) return null;

                long joinTime = resultSet.getLong("join_time");

                return new PlayerData(joinTime);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Saves the {@link PlayerData} for the {@link UUID} provided.
     * @param uuid The {@link UUID} the {@link PlayerData} belongs to.
     * @param playerData The {@link PlayerData} to save.
     */
    public void savePlayerData(@NotNull UUID uuid, @NotNull PlayerData playerData) {
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (player_id, join_time, last_updated) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (player_id) DO UPDATE SET " +
                "join_time = ?, last_updated = ? WHERE last_updated < ?";

        UUIDParameter playerIdParameter = new UUIDParameter(uuid);
        LongParameter joinTimeParameter = new LongParameter(playerData.joinTime());
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        queueManager.queueWriteTransaction(insertOrUpdateSql,
                List.of(
                        playerIdParameter,
                        joinTimeParameter,
                        lastUpdatedParameter,
                        joinTimeParameter,
                        lastUpdatedParameter,
                        lastUpdatedParameter));
    }
}
