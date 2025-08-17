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
package com.github.lukesky19.newPlayerPerks;

import com.github.lukesky19.newPlayerPerks.data.Settings;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.PlayerDataManager;
import com.github.lukesky19.newPlayerPerks.manager.config.SettingsManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class acts as the API for NewPlayerPerks.
 */
public class NewPlayerPerksAPI {
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull PerksManager perksManager;

    /**
     * Constructor
     * @param settingsManager A {@link SettingsManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param perksManager A {@link PerksManager} instance.
     */
    public NewPlayerPerksAPI(
            @NotNull SettingsManager settingsManager,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull PerksManager perksManager) {
        this.settingsManager = settingsManager;
        this.playerDataManager = playerDataManager;
        this.perksManager = perksManager;
    }

    /**
     * Does the player have perks, regardless if enabled or not?
     * @param uuid The {@link UUID} of the player.
     * @return true if enabled, otherwise false.
     */
    public boolean hasPerks(@NotNull UUID uuid) {
        return perksManager.doesPlayerHavePerks(uuid);
    }

    /**
     * Does the player have perks enabled?
     * @param uuid The {@link UUID} of the player.
     * @return true if enabled, otherwise false.
     */
    public boolean hasPerksEnabled(@NotNull UUID uuid) {
        return playerDataManager.getActivePerksMap().containsKey(uuid);
    }

    /**
     * Is making the player invulnerable an enabled perk in the plugin's settings?
     * If the plugin's settings are invalid, this will always return false.
     * @return true if enabled, otherwise false.
     */
    public boolean isInvulnerablePerkEnabled() {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return false;

        return settings.invulnerable();
    }

    /**
     * Is the void teleportation an enabled perk in the plugin's settings?
     * If the plugin's settings are invalid, this will always return false.
     * @return true if enabled, otherwise false.
     */
    public boolean isVoidTeleportPerkEnabled() {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return false;

        return settings.voidTeleport();
    }

    /**
     * Is giving the essentials fly permission an enabled perk in the plugin's settings?
     * If the plugin's settings are invalid, this will always return false.
     * @return true if enabled, otherwise false.
     */
    public boolean isEssentialsFlyPerkEnabled() {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return false;

        return settings.essentialsFly();
    }

    /**
     * Is giving the island fly permission an enabled perk in the plugin's settings?
     * If the plugin's settings are invalid, this will always return false.
     * @return true if enabled, otherwise false.
     */
    public boolean isIslandFlyPerkEnabled() {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return false;

        return settings.islandFly();
    }

    /**
     * Is the keep inventory enabled an enabled in the plugin's settings?
     * If the plugin's settings are invalid, this will always return false.
     * @return true if enabled, otherwise false.
     */
    public boolean isKeepInventoryPerkEnabled() {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return false;

        return settings.keepInventory();
    }

    /**
     * Is the keep experience enabled an enabled in the plugin's settings?
     * If the plugin's settings are invalid, this will always return false.
     * @return true if enabled, otherwise false.
     */
    public boolean isKeepExpPerkEnabled() {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return false;

        return settings.keepExp();
    }
}
