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
import com.github.lukesky19.newPlayerPerks.configuration.locale.LocaleManager;
import com.github.lukesky19.newPlayerPerks.configuration.settings.Settings;
import com.github.lukesky19.newPlayerPerks.configuration.settings.SettingsManager;
import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
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

    private @Nullable BukkitTask checkPerksTask;

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
     * @param player The {@link Player} to check.
     * @return true or false.
     */
    public boolean doesPlayerHavePerks(@NotNull Player player) {
        if(settingsManager.getPeriod() == null) {
            logger.error(AdventureUtil.serialize("Unable to check if player has perks due to an invalid period in settings.yml."));
            return false;
        }

        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if(playerData == null) return false;

        return System.currentTimeMillis() < (playerData.joinTime() + settingsManager.getPeriod());
    }

    /**
     * Start the {@link BukkitTask} that checks perks.
     */
    public void startCheckPerksTask() {
        checkPerksTask = newPlayerPerks.getServer().getScheduler().runTaskTimer(newPlayerPerks, this::checkPerks, 20L, 20L);
    }

    /**
     * Stop the {@link BukkitTask} that checks perks.
     */
    public void stopCheckPerksTask() {
        if(checkPerksTask != null && !checkPerksTask.isCancelled()) {
            checkPerksTask.cancel();
            checkPerksTask = null;
        }
    }

    /**
     * Checks whether players with perks need them removed or not.
     */
    private void checkPerks() {
        if(settingsManager.getPeriod() == null) {
            logger.error(AdventureUtil.serialize("Unable to check perks should be removed due to an invalid period in settings.yml."));
            return;
        }

        for(Map.Entry<UUID, PlayerData> entry : playerDataManager.getPlayerDataMap().entrySet()) {
            UUID uuid = entry.getKey();
            PlayerData playerData = entry.getValue();

            if(System.currentTimeMillis() > (playerData.joinTime() + settingsManager.getPeriod())) {
                Player player = newPlayerPerks.getServer().getPlayer(uuid);
                if(player != null && player.isOnline() && player.isConnected()) {
                    removePerks(player, uuid, true);
                }
            }
        }
    }

    /**
     * Applies configured perks to the player provided.
     * @param player The {@link Player}.
     * @param uuid The {@link UUID} of the player.
     */
    public void applyPerks(@NotNull Player player, @NotNull UUID uuid) {
        // Get Plugin Settings
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        // Get LuckPerms User
        UserManager userManager = newPlayerPerks.getLuckPermsAPI().getUserManager();
        User user = userManager.getUser(uuid);
        if(user != null) {
            // Invulnerable
            if(settings.invulnerable()) {
                player.setInvulnerable(true);
            }

            // Fly
            if(settings.fly()) {
                PermissionNode eFly = PermissionNode.builder("essentials.fly").value(true).build();
                PermissionNode iFly = PermissionNode.builder("bskyblock.island.fly").value(true).build();
                Objects.requireNonNull(user).data().add(eFly);
                user.data().add(iFly);
                player.setAllowFlight(true);
                player.setFlying(true);
            }

            // NOTE: Keep Inventory and Keep Exp is checked on Death.

            // Void Teleport
            if(settings.voidTeleport()) {
                PermissionNode voidTele = PermissionNode.builder("bskyblock.voidteleport").value(true).build();
                user.data().add(voidTele);
            }

            // Save modified User
            userManager.saveUser(user);
        }
    }

    /**
     * Removes configured perks to the player provided.
     * @param player The {@link Player}.
     * @param uuid The {@link UUID} of the player.
     * @param sendMessage Whether to send the configured messages for when perks expire or not.
     */
    public void removePerks(@NotNull Player player, @NotNull UUID uuid, boolean sendMessage) {
        // Get LuckPerms User
        UserManager userManager = newPlayerPerks.getLuckPermsAPI().getUserManager();
        User user = userManager.getUser(uuid);

        // Give Fly
        PermissionNode eFly = PermissionNode.builder("essentials.fly").build();
        PermissionNode iFly = PermissionNode.builder("bskyblock.island.fly").build();
        Objects.requireNonNull(user).data().remove(eFly);
        user.data().remove(iFly);
        player.setAllowFlight(false);
        player.setFlying(false);

        // Give Void Teleport
        PermissionNode voidTele = PermissionNode.builder("bskyblock.voidteleport").build();
        user.data().remove(voidTele);

        // Invulnerable
        player.setInvulnerable(false);

        // Save modified User
        userManager.saveUser(user);

        if(sendMessage) {
            for(String msg : localeManager.getLocale().perksExpireMessages()) {
                player.sendMessage(AdventureUtil.serialize(player, localeManager.getLocale().prefix() + msg));
            }
        }
    }
}
