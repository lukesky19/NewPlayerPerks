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
package com.github.lukesky19.newPlayerPerks.manager;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.newPlayerPerks.manager.database.DatabaseManager;
import com.github.lukesky19.newPlayerPerks.manager.database.tables.PlayerDataTable;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * This class manages access to player data.
 */
public class PlayerDataManager {
    private final @NotNull NewPlayerPerks newPlayerPerks;
    private final @NotNull DatabaseManager databaseManager;

    private final @NotNull Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final @NotNull Map<UUID, PlayerData> activePerksPlayerDataMap = new HashMap<>();

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     */
    public PlayerDataManager(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull DatabaseManager databaseManager) {
        this.newPlayerPerks = newPlayerPerks;
        this.databaseManager = databaseManager;
    }

    /**
     * Get the {@link PlayerData} for the {@link UUID} provided.
     * @param uuid The {@link UUID} for the player.
     * @return The {@link PlayerData}. May be null.
     */
    public @Nullable PlayerData getPlayerData(@NotNull UUID uuid) {
        return playerDataMap.get(uuid);
    }

    /**
     * Get the {@link Map} mapping {@link UUID}s to {@link PlayerData}.
     * @return The {@link Map} mapping {@link UUID}s to {@link PlayerData}.
     */
    public @NotNull Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    /**
     * Stores the player's player data in the active perks map.
     * The active perks map is iterated over to check if their perks have expired to remove them.
     * @param uuid The {@link UUID} of the player.
     */
    public void addToActivePerksMap(@NotNull UUID uuid) {
        PlayerData playerData = playerDataMap.get(uuid);
        if(playerData == null) return;

        activePerksPlayerDataMap.put(uuid, playerData);
    }

    /**
     * Remove the player's player data from the active perks map.
     * @param uuid The {@link UUID} of the player.
     */
    public void removeFromActivePerksMap(@NotNull UUID uuid) {
        activePerksPlayerDataMap.remove(uuid);
    }

    /**
     * Get the {@link Map} mapping {@link UUID}s to {@link PlayerData} that have perks enabled.
     * @return The {@link Map} mapping {@link UUID}s to {@link PlayerData} that have perks enabled.
     */
    public @NotNull Map<UUID, PlayerData> getActivePerksMap() {
        return activePerksPlayerDataMap;
    }

    /**
     * Reload player data.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> reload() {
        return savePlayerData().thenCompose(v1 -> {
            playerDataMap.clear();
            activePerksPlayerDataMap.clear();

            return migrateLegacyPlayerData().thenCompose(v2 -> loadPlayerData());
        });
    }

    /**
     * (Re-)loads player data from the database.
     * Existing player data that is stored will be cleared.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> loadPlayerData() {
        playerDataMap.clear();

        List<CompletableFuture<PlayerData>> futuresList = new ArrayList<>();
        newPlayerPerks.getServer().getOnlinePlayers().forEach(player ->
                futuresList.add(loadPlayerData(player.getUniqueId())));

        return CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[0]));
    }

    /**
     * Get the {@link PlayerData} from the database. If no data exists, then a new {@link PlayerData} record will be created.
     * If the plugin's settings are invalid, the returned {@link PlayerData} will be null.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} containing {@link PlayerData}.
     */
    public @NotNull CompletableFuture<@NotNull PlayerData> loadPlayerData(@NotNull UUID uuid) {
        ComponentLogger logger = newPlayerPerks.getComponentLogger();
        PlayerDataTable playerDataTable = databaseManager.getPlayerDataTable();

        return playerDataTable.loadPlayerData(uuid).thenApply(playerData -> {
            if(playerData == null) {
                PlayerData newPlayerData = new PlayerData();

                playerDataMap.put(uuid, newPlayerData);

                playerDataTable.savePlayerData(uuid, newPlayerData);

                return newPlayerData;
            }

            playerDataMap.put(uuid, playerData);

            return playerData;
        }).exceptionally(throwable -> {
            if(throwable != null) {
                logger.error(AdventureUtil.serialize("Loading of player data failed: " + throwable.getMessage()));
            }

            return null;
        });
    }

    /**
     * Unload the player data for the {@link UUID} provided.
     * @param uuid The {@link UUID} of the player.
     */
    public void unloadPlayerData(@NotNull UUID uuid) {
        activePerksPlayerDataMap.remove(uuid);
        playerDataMap.remove(uuid);
    }

    /**
     * Save the {@link PlayerData} for the {@link UUID} provided.
     * @param uuid The {@link UUID} of the player.
     * @param playerData The {@link PlayerData}.
     */
    public void savePlayerData(@NotNull UUID uuid, PlayerData playerData) {
        PlayerDataTable playerDataTable = databaseManager.getPlayerDataTable();
        playerDataTable.savePlayerData(uuid, playerData);

        playerDataMap.put(uuid, playerData);
    }

    /**
     * Save the {@link PlayerData} for  all loaded player data.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> savePlayerData() {
        PlayerDataTable playerDataTable = databaseManager.getPlayerDataTable();

        return playerDataTable.savePlayerData(playerDataMap);
    }

    /**
     * Loads and migrates all legacy player data and saves the updated player data to the database.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> migrateLegacyPlayerData() {
        ComponentLogger logger = newPlayerPerks.getComponentLogger();

        try {
            PlayerDataTable playerDataTable = databaseManager.getPlayerDataTable();

            Path playerDataPath = Path.of(newPlayerPerks.getDataFolder() + File.separator + "playerdata");
            // If the path is not a directory, don't migrate any data.
            if (!Files.isDirectory(playerDataPath)) return CompletableFuture.completedFuture(null);

            // Don't migrate player data if the path's directory doesn't exist.
            if (!Files.exists(playerDataPath)) return CompletableFuture.completedFuture(null);

            List<CompletableFuture<Void>> futureList = new ArrayList<>();

            try (Stream<Path> paths = Files.walk(playerDataPath)) {
                paths.filter(Files::isRegularFile)
                        .forEach(path -> {
                            String fileName = path.toFile().getName();
                            String nameWithoutExtension = fileName.replaceAll("\\.yml$", "");
                            UUID uuid = UUID.fromString(nameWithoutExtension);

                            @NotNull YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                            try {
                                PlayerData playerData = loader.load().get(PlayerData.class);
                                if (playerData != null) {
                                    futureList.add(playerDataTable.savePlayerData(uuid, playerData));
                                }

                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    logger.warn(AdventureUtil.serialize("Failed to delete legacy player data for file: " + path.toFile() + ". Error: " + e.getMessage()));
                                }
                            } catch (ConfigurateException e) {
                                logger.warn(AdventureUtil.serialize("Failed to migrate legacy player data for file: " + path.toFile() + ". Error: " + e.getMessage()));
                            }
                        });
            } catch (IOException e) {
                logger.warn(AdventureUtil.serialize("Failed to migrate legacy player data. Error: " + e.getMessage()));
                return CompletableFuture.completedFuture(null);
            }

            // If the player data folder is empty, delete the directory
            try (Stream<Path> paths = Files.list(playerDataPath)) {
                int count = paths.toList().size();
                if (count == 0) {
                    Files.delete(playerDataPath);
                }
            } catch (IOException e) {
                logger.error(AdventureUtil.serialize(e.getMessage()));
                return CompletableFuture.completedFuture(null);
            }

            return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        } catch (RuntimeException e) {
            logger.error(AdventureUtil.serialize(e.getMessage()));
            return CompletableFuture.completedFuture(null);
        }
    }
}
