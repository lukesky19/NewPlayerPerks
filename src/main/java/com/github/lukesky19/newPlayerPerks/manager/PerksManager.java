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
import com.github.lukesky19.newPlayerPerks.data.Locale;
import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.newPlayerPerks.data.Settings;
import com.github.lukesky19.newPlayerPerks.manager.config.LocaleManager;
import com.github.lukesky19.newPlayerPerks.manager.config.SettingsManager;
import com.github.lukesky19.newPlayerPerks.util.PerksResult;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * This class manages perks that are applied or removed from players.
 */
public class PerksManager {
    private final @NotNull NewPlayerPerks newPlayerPerks;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PlayerDataManager playerDataManager;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public PerksManager(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull PlayerDataManager playerDataManager) {
        this.newPlayerPerks = newPlayerPerks;
        this.logger = newPlayerPerks.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.playerDataManager = playerDataManager;
    }

    /**
     * Based on the player's join time, does the player have perks.
     * @param uuid The {@link UUID} of the player.
     * @return true or false.
     */
    public boolean doesPlayerHavePerks(@NotNull UUID uuid) {
        if(settingsManager.getPeriod() == null) {
            logger.error(AdventureUtil.serialize("Unable to check if player has perks due to an invalid period in settings.yml."));
            return false;
        }

        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        if(playerData == null) return false;

        return System.currentTimeMillis() < (playerData.getJoinTime() + settingsManager.getPeriod());
    }

    /**
     * Enable the perks for the player.
     * @param player The {@link Player}.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link PerksResult}.
     */
    public @NotNull PerksResult enablePerks(@NotNull Player player, @NotNull UUID uuid) {
        // Get Plugin Settings
        Settings settings = settingsManager.getSettings();
        if(settings == null) return PerksResult.SETTINGS_ERROR;
        if(settingsManager.getPeriod() == null) return PerksResult.SETTINGS_ERROR;

        // Get PlayerData
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        if(playerData == null) return PerksResult.NO_PLAYER_DATA;
        // Check if perks can be applied
        if(System.currentTimeMillis() > (playerData.getJoinTime() + settingsManager.getPeriod())) return PerksResult.EXPIRED;

        // Get LuckPerms User
        UserManager userManager = newPlayerPerks.getLuckPermsAPI().getUserManager();
        User user = userManager.getUser(uuid);
        if(user == null) return PerksResult.USER_ERROR;
        NodeMap userData = user.data();

        setPerks(settings, userManager, player, user, userData);

        playerDataManager.addToActivePerksMap(uuid);

        return PerksResult.SUCCESS;
    }

    /**
     * Disable the perks for the player.
     * @param player The {@link Player}.
     * @param uuid The {@link UUID} of the player.
     * @param expireCheck Should it be checked if perks have expired before disabling perks?
     * @return A {@link PerksResult}.
     */
    public @NotNull PerksResult disablePerks(@NotNull Player player, @NotNull UUID uuid, boolean expireCheck) {
        // Get Plugin Settings
        Settings settings = settingsManager.getSettings();
        if(settings == null) return PerksResult.SETTINGS_ERROR;
        if(settingsManager.getPeriod() == null) return PerksResult.SETTINGS_ERROR;

        if(expireCheck) {
            // Get PlayerData
            PlayerData playerData = playerDataManager.getPlayerData(uuid);
            if(playerData == null) return PerksResult.NO_PLAYER_DATA;
            // Check if perks can be applied
            if(System.currentTimeMillis() > (playerData.getJoinTime() + settingsManager.getPeriod())) return PerksResult.EXPIRED;
        }

        // Get LuckPerms User
        UserManager userManager = newPlayerPerks.getLuckPermsAPI().getUserManager();
        User user = userManager.getUser(uuid);
        if(user == null) return PerksResult.USER_ERROR;
        NodeMap userData = user.data();

        unsetPerks(settings, userManager, player, user, userData);

        playerDataManager.removeFromActivePerksMap(uuid);

        return PerksResult.SUCCESS;
    }

    /**
     * Add perks to the player by modifying their join time and then enabling perks.
     * Use {@link #enablePerks(Player, UUID)} to enable perks based on the player's current join time.
     * @param player The {@link Player}.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link PerksResult}.
     */
    public @NotNull PerksResult applyPerks(@NotNull Player player, @NotNull UUID uuid) {
        // Get PlayerData
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        if(playerData == null) return PerksResult.NO_PLAYER_DATA;

        // Set join time to the current system time
        playerData.setJoinTime(System.currentTimeMillis());

        playerDataManager.savePlayerData(uuid, playerData);

        // Enable perks and return result
        return enablePerks(player, uuid);
    }

    /**
     * Removes configured perks from the player provided.
     * @param player The {@link Player}.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link PerksResult}.
     */
    public @NotNull PerksResult removePerks(@NotNull Player player, @NotNull UUID uuid) {
        // Get PlayerData
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        if(playerData == null) return PerksResult.NO_PLAYER_DATA;

        // Set join time to the current system time
        playerData.setJoinTime(0);

        playerDataManager.savePlayerData(uuid, playerData);

        return disablePerks(player, uuid, false);
    }

    /**
     * Enables perks for all perks that should have perks enabled based on their join time.
     */
    public void enableAllPerks() {
        if(settingsManager.getPeriod() == null) {
            logger.error(AdventureUtil.serialize("Unable to check if perks should be applied due to an invalid period in settings.yml."));
            return;
        }

        Server server = newPlayerPerks.getServer();

        playerDataManager.getPlayerDataMap().forEach((uuid, playerData) -> {
            Player player = server.getPlayer(uuid);
            if(player != null && player.isOnline() && player.isConnected()) {
                PerksResult perksResult = enablePerks(player, uuid);

                switch(perksResult) {
                    case SUCCESS -> {
                        List<TagResolver.Single> placeholders = List.of(
                                Placeholder.parsed("expire_time", TimeUtil.millisToTimeStamp((playerData.getJoinTime() + settingsManager.getPeriod()), ZoneId.of("America/New_York"), "MM-dd-yyyy HH:mm:ss z")),
                                Placeholder.parsed("remaining_time", localeManager.getTimeMessage((playerData.getJoinTime() + settingsManager.getPeriod()) - System.currentTimeMillis())));

                        for(String msg : localeManager.getLocale().perksEnabledMessages()) {
                            player.sendMessage(AdventureUtil.serialize(player, localeManager.getLocale().prefix() + msg, placeholders));
                        }
                    }

                    case SETTINGS_ERROR -> logger.error(AdventureUtil.serialize("Unable to apply perks due invalid plugin settings."));

                    case NO_PLAYER_DATA -> logger.error(AdventureUtil.serialize("Unable to apply perks due no player data found for the player " + player.getName() + "."));

                    case USER_ERROR -> logger.error(AdventureUtil.serialize("Unable to apply perks due LuckPerms user found for the player " + player.getName() + "."));

                    default -> {}
                }
            }
        });
    }

    /**
     * Disables perks for players that have had perks applied.
     * Does not remove them, just disables them.
     * @param onReload Are perks being removed due to a reload?
     */
    public void disableAllPerks(boolean onReload) {
        Locale locale = localeManager.getLocale();
        Server server = newPlayerPerks.getServer();

        playerDataManager.getActivePerksMap()
            .forEach((uuid, playerData) -> {
                Player player = server.getPlayer(uuid);
                if(player != null && player.isOnline() && player.isConnected()) {
                    disablePerks(player, uuid, false);

                    if(onReload) player.sendMessage(locale.prefix() + locale.disablePerksReload());
                }
            });
    }


    /**
     * Set the perks based on the plugin's settings.
     * @param settings The plugin's {@link Settings}.
     * @param userManager LuckPerm's {@link UserManager}.
     * @param player The {@link Player} to apply perks to.
     * @param user The LuckPerm's {@link User} to apply perks to.
     * @param userData The user's data from LuckPerms, See {@link User#data()}.
     */
    private void setPerks(@NotNull Settings settings, @NotNull UserManager userManager, @NotNull Player player, @NotNull User user, @NotNull NodeMap userData) {
        // Invulnerable
        if(settings.invulnerable()) {
            player.setInvulnerable(true);
        }

        // Fly
        if(settings.essentialsFly()) {
            PermissionNode eFly = PermissionNode.builder("essentials.fly").value(true).build();
            userData.add(eFly);
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        if(settings.islandFly()) {
            PermissionNode iFly = PermissionNode.builder("bskyblock.island.fly").value(true).build();
            userData.add(iFly);
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        // NOTE: Keep Inventory and Keep Exp is checked on Death.

        // Void Teleport
        if(settings.voidTeleport()) {
            PermissionNode voidTele = PermissionNode.builder("bskyblock.voidteleport").value(true).build();
            userData.add(voidTele);
        }

        // Save modified User
        userManager.saveUser(user);
    }

    /**
     * Remove the perks based on the plugin's settings.
     * @param settings The plugin's {@link Settings}.
     * @param userManager LuckPerm's {@link UserManager}.
     * @param player The {@link Player} to remove perks from.
     * @param user The LuckPerm's {@link User} to remove perks from.
     * @param userData The user's data from LuckPerms, See {@link User#data()}.
     */
    private void unsetPerks(@NotNull Settings settings, @NotNull UserManager userManager, @NotNull Player player, @NotNull User user, @NotNull NodeMap userData) {
        // Invulnerable
        if(settings.invulnerable()) {
            player.setInvulnerable(false);
        }

        // Fly
        if(settings.essentialsFly()) {
            PermissionNode eFly = PermissionNode.builder("essentials.fly").build();
            userData.remove(eFly);
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        if(settings.islandFly()) {
            PermissionNode iFly = PermissionNode.builder("bskyblock.island.fly").build();
            userData.remove(iFly);
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        // Void Teleport
        if(settings.voidTeleport()) {
            PermissionNode voidTele = PermissionNode.builder("bskyblock.voidteleport").build();
            userData.remove(voidTele);
        }

        // Save modified User
        userManager.saveUser(user);
    }
}
