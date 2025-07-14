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
package com.github.lukesky19.newPlayerPerks.configuration.locale;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.configuration.settings.Settings;
import com.github.lukesky19.newPlayerPerks.configuration.settings.SettingsManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages the plugin's locale configuration.
 */
public class LocaleManager {
    private final @NotNull NewPlayerPerks newPlayerPerks;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull Locale DEFAULT_LOCALE = new Locale(
            "<aqua><bold>NewPlayerPerks</bold></aqua><gray> ▪ </gray>",
            "<green>Configuration files have been reloaded.</green>",
            "<green>Perks have been successfully added to this player.</green>",
            "<green>Perks have been successfully removed from this player.</green>",
            List.of(
                    "<green>For the next 6 hours, you are now invulnerable, have keep inventory, and will be teleported to your island if you fall into the void.</green>",
                    "<green>Use this time to get a jump start on your island and get to know the server.</green>"),
            List.of(
                    "<red>Your invulnerability, keep inventory, void teleport perks have expired!</red>",
                    "<red>You can now take damage, die, and you won't be teleported to your island if you fall into the void!</red>"));
    private @Nullable Locale locale;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param settingsManager A {@link SettingsManager} intsance.
     */
    public LocaleManager(@NotNull NewPlayerPerks newPlayerPerks, @NotNull SettingsManager settingsManager) {
        this.newPlayerPerks = newPlayerPerks;
        this.settingsManager = settingsManager;
    }

    /**
     * Get the plugin's {@link Locale} or the default {@link Locale} if the user-configured version failed to load.
     * @return The {@link Locale}.
     */
    public @NotNull Locale getLocale() {
        if(locale == null) return DEFAULT_LOCALE;

        return locale;
    }

    /**
     * Reloads the plugin's locale.
     */
    public void reload() {
        ComponentLogger logger = newPlayerPerks.getComponentLogger();
        locale = null;

        copyDefaultLocales();

        Settings settings = settingsManager.getSettings();
        if(settings == null) {
            logger.error(AdventureUtil.serialize("Unable to load the plugin's locale due to invalid plugin settings. The default locale will be used."));
            return;
        }
        if(settings.locale() == null) {
            logger.error(AdventureUtil.serialize("Unable to load the plugin's locale due to a locale not being configured in settings.yml. The default locale will be used."));
            return;
        }

        Path path = Path.of(
                newPlayerPerks.getDataFolder()
                        + File.separator
                        + "locale"
                        + File.separator
                        + settings.locale()
                        + ".yml");
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);

        try {
            locale = loader.load().get(Locale.class);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.serialize("Failed to load the locale configuration. Error: " + e.getMessage()));
        }
    }

    /**
     * Copies the default locale files that come bundled with the plugin, if they do not exist at least.
     */
    private void copyDefaultLocales() {
        Path path = Path.of(newPlayerPerks.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            newPlayerPerks.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }
}
