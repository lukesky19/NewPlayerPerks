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
package com.github.lukesky19.newPlayerPerks.database.tables;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.newPlayerPerks.database.QueueManager;
import com.github.lukesky19.newPlayerPerks.settings.SettingsManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.database.parameter.Parameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.IntegerParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.LongParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.UUIDParameter;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages access to the player data table in the database.
 */
public class PlayerDataTable {
    private final @NotNull NewPlayerPerks newPlayerPerks;
    private final @NotNull ComponentLogger logger;
    private final @NotNull QueueManager queueManager;
    private final @NotNull VersionsTable versionsTable;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull String tableName = "newplayerperks_player_data";

    /**
     * Default Constructor.
     * You should use {@link #PlayerDataTable(NewPlayerPerks, QueueManager, VersionsTable, SettingsManager)} instead.
     * @deprecated You should use {@link #PlayerDataTable(NewPlayerPerks, QueueManager, VersionsTable, SettingsManager)} instead.
     */
    @Deprecated
    public PlayerDataTable() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param queueManager A {@link QueueManager} instance.
     * @param versionsTable A {@link VersionsTable} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public PlayerDataTable(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull QueueManager queueManager,
            @NotNull VersionsTable versionsTable,
            @NotNull SettingsManager settingsManager) {
        this.newPlayerPerks = newPlayerPerks;
        this.logger = newPlayerPerks.getComponentLogger();
        this.queueManager = queueManager;
        this.versionsTable = versionsTable;
        this.settingsManager = settingsManager;
    }

    /**
     * Creates the table in the database if it doesn't exist and any indexes that don't exist.
     */
    @SuppressWarnings("CodeBlock2Expr")
    public void createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "player_id LONG NOT NULL UNIQUE DEFAULT 0, " +
                "perk_time LONG NOT NULL DEFAULT 0, " +
                "join_time LONG NOT NULL DEFAULT 0, " +
                "paused INTEGER NOT NULL DEFAULT 0, " +
                "last_updated LONG NOT NULL DEFAULT 0)";
        String playerIdsIndexSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_player_ids ON " + tableName + "(player_id)";

        String tableExistsSql = "SELECT EXISTS(SELECT 1 FROM sqlite_master WHERE type='table' AND name='" + tableName + "') AS table_exists";
        queueManager.queueReadTransaction(tableExistsSql, resultSet -> {
            try {
                int exists = 0;

                if(resultSet.next()) exists = resultSet.getInt(1);

                return exists == 1;
            } catch (SQLException e) {
                logger.error(AdventureUtil.deserialize("Failed to check if the player data table exists."));
                return null;
            }
        }).thenAccept(tableExists -> {
            if(tableExists == null) return;

            if(tableExists) {
                versionsTable.getTableVersion(tableName).thenAccept(version -> {
                    if(version <= 0) {
                        if(settingsManager.getPeriod() == null) {
                            logger.error(AdventureUtil.deserialize("Unable to migrate player data table due to invalid plugin settings."));

                            newPlayerPerks.getServer().getScheduler().runTaskLater(newPlayerPerks, () ->
                                    newPlayerPerks.getServer().getPluginManager().disablePlugin(newPlayerPerks), 1L);

                            return;
                        }

                        loadPlayerData(settingsManager.getPeriod()).thenCompose(playerDataMap -> {
                            return queueManager.queueWriteTransaction("DROP TABLE " + tableName).thenCompose(result -> {
                                return queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, playerIdsIndexSql)).thenCompose(v1 ->  {
                                    return versionsTable.updateVersion(tableName, 1).thenCompose(v2 -> {
                                        return savePlayerData(playerDataMap);
                                    });
                                });
                            });
                        }).exceptionally(ex -> {
                            logger.error(AdventureUtil.deserialize("Failed to migrate player data table from version 0 to version 1. Error: " + ex.getMessage()));
                            return null;
                        });
                    } else {
                        queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, playerIdsIndexSql))
                                .thenAccept(v1 -> versionsTable.updateVersion(tableName, 1))
                                .exceptionally(ex -> {
                                    logger.error(AdventureUtil.deserialize("Failed to create the player data table."));
                                    return null;
                                });
                    }
                });
            } else {
                queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, playerIdsIndexSql))
                        .thenAccept(v1 -> versionsTable.updateVersion(tableName, 1))
                        .exceptionally(ex -> {
                            logger.error(AdventureUtil.deserialize("Failed to create the player data table."));
                            return null;
                        });
            }
        }).exceptionally(ex -> {
            logger.error(AdventureUtil.deserialize("Failed to check if the player data table exists."));
            return null;
        });
    }

    /**
     * Loads the {@link PlayerData} for the {@link UUID} provided.
     * @param player The {@link Player}.
     * @param playerId The {@link UUID} of the player.
     * @return A {@link CompletableFuture} containing {@link PlayerData}. May be null.
     */
    public @NotNull CompletableFuture<@Nullable PlayerData> loadPlayerData(@NotNull Player player, @NotNull UUID playerId) {
        String selectSql = "SELECT perk_time, join_time, paused FROM " + tableName + " WHERE player_id = ? AND last_updated < ?";
        UUIDParameter uuidParameter = new UUIDParameter(playerId);
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        return queueManager.queueReadTransaction(selectSql, List.of(uuidParameter, lastUpdatedParameter), resultSet -> {
            try {
                if(!resultSet.next()) return null;
                long joinTime = resultSet.getLong("join_time");
                if(joinTime == 0) joinTime = player.getFirstPlayed();

                return new PlayerData(playerId, resultSet.getLong("perk_time"), joinTime, resultSet.getBoolean("paused"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Load all data from the legacy (version 0) table.
     * @param period The time in seconds perks last for.
     * @return A {@link CompletableFuture} containing a {@link Map} mapping {@link UUID} to {@link PlayerData}.
     */
    private @NotNull CompletableFuture<Map<UUID, PlayerData>> loadPlayerData(long period) {
        String selectSql = "SELECT player_id, join_time FROM " + tableName;

        return queueManager.queueReadTransaction(selectSql, resultSet -> {
            Map<UUID, PlayerData> playerDataMap = new HashMap<>();

            try {
                while(resultSet.next()) {
                    UUID playerId = UUID.fromString(resultSet.getString("player_id"));
                    long joinTime = resultSet.getLong("join_time");
                    long perkTime = Math.max(0, (joinTime + period) - System.currentTimeMillis());

                    playerDataMap.put(playerId, new PlayerData(playerId, perkTime, joinTime, false));
                }

                return playerDataMap;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Saves the {@link PlayerData} for the {@link UUID} provided.
     * @param uuid The {@link UUID} the {@link PlayerData} belongs to.
     * @param playerData The {@link PlayerData} to save.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> savePlayerData(@NotNull UUID uuid, @NotNull PlayerData playerData) {
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (player_id, perk_time, join_time, paused, last_updated) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (player_id) DO UPDATE SET " +
                "perk_time = ?, join_time = ?, paused = ?, last_updated = ? WHERE last_updated < ?";

        UUIDParameter playerIdParameter = new UUIDParameter(uuid);
        LongParameter perkTimeParameter = new LongParameter(playerData.getPerkTime());
        LongParameter joinTimeParameter = new LongParameter(playerData.getJoinTime());
        IntegerParameter pausedParameter = new IntegerParameter(playerData.isPerksPaused() ? 1 : 0);
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        return queueManager.queueWriteTransaction(insertOrUpdateSql,
                List.of(
                        playerIdParameter,
                        perkTimeParameter,
                        joinTimeParameter,
                        pausedParameter,
                        lastUpdatedParameter,
                        perkTimeParameter,
                        joinTimeParameter,
                        pausedParameter,
                        lastUpdatedParameter,
                        lastUpdatedParameter)).thenRun(() -> {});
    }

    /**
     * Saves all data in the {@link Map} mapping {@link UUID}s to {@link PlayerData} provided.
     * @param playerDataMap The {@link Map} mapping {@link UUID}s to {@link PlayerData} to save.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> savePlayerData(@NotNull Map<UUID, PlayerData> playerDataMap) {
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (player_id, perk_time, join_time, paused, last_updated) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (player_id) DO UPDATE SET " +
                "perk_time = ?, join_time = ?, paused = ?, last_updated = ? WHERE last_updated < ?";

        List<List<Parameter<?>>> listOfParameterLists = new ArrayList<>();

        playerDataMap.forEach((uuid, playerData) -> {
            UUIDParameter playerIdParameter = new UUIDParameter(uuid);
            LongParameter perkTimeParameter = new LongParameter(playerData.getPerkTime());
            LongParameter joinTimeParameter = new LongParameter(playerData.getJoinTime());
            IntegerParameter pausedParameter = new IntegerParameter(playerData.isPerksPaused() ? 1 : 0);
            LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

            listOfParameterLists.add(
                    List.of(
                            playerIdParameter,
                            perkTimeParameter,
                            joinTimeParameter,
                            pausedParameter,
                            lastUpdatedParameter,
                            perkTimeParameter,
                            joinTimeParameter,
                            pausedParameter,
                            lastUpdatedParameter,
                            lastUpdatedParameter));
        });

        if(listOfParameterLists.isEmpty()) return CompletableFuture.completedFuture(null);

        return queueManager.queueBulkWriteTransaction(insertOrUpdateSql, listOfParameterLists).thenRun(() -> {});
    }
}