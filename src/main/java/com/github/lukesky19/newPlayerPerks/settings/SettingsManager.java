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
package com.github.lukesky19.newPlayerPerks.settings;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.time.TimeUtil;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * This class manages the plugin's settings.
 */
public class SettingsManager {
    private final @NotNull NewPlayerPerks newPlayerPerks;
    private @Nullable Settings settings;
    private @Nullable Long period;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     */
    public SettingsManager(@NotNull NewPlayerPerks newPlayerPerks) {
        this.newPlayerPerks = newPlayerPerks;
    }

    /**
     * Get the plugin's {@link Settings}.
     * @return The plugin's {@link Settings} or null.
     */
    public @Nullable Settings getSettings() {
        return settings;
    }

    /**
     * Get the number of seconds that new player perks should last for.
     * @return The number of seconds or null.
     */
    public @Nullable Long getPeriod() {
        return period;
    }

    /**
     * Reloads the plugin's settings.
     */
    public void reload() {
        ComponentLogger logger = newPlayerPerks.getComponentLogger();
        Path path = Path.of(newPlayerPerks.getDataFolder() + File.separator + "settings.yml");

        if(!path.toFile().exists()) {
            newPlayerPerks.saveResource("settings.yml", false);
        }

        YamlConfigurationLoader loader = createLoader(path);
        try {
            settings = loader.load().get(Settings.class);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtility.plain("Unable to load plugin settings due to an error: " + e.getMessage()));
            return;
        }

        migrateSettings();

        if(settings == null) return;
        if(settings.period() == null) return;

        period = TimeUtil.stringToMillis(settings.period());
        period = Math.max(0, period / 1000);
    }

    /**
     * Migrate the plugin's settings configuration.
     */
    private void migrateSettings() {
        if(settings == null) return;

        switch(settings.configVersion()) {
            case "1.1.1.0" -> {
                // Current version, do nothing
            }

            case "1.1.0.0" -> {
                // 1.1.0.0 -> 1.1.1.0
                settings = new Settings("1.1.1.0", settings.locale(), settings.invulnerable(), null, settings.essentialsFly(), settings.islandFly(), false, settings.keepInventory(), settings.keepExp(), settings.voidTeleport(), settings.period());

                saveSettings();
            }

            case null -> {
                // 1.0.0.0 -> 1.1.0.0
                boolean flySetting = Objects.requireNonNullElse(settings.fly(), false);
                settings = new Settings("1.1.0.0", settings.locale(), settings.invulnerable(), null, flySetting, flySetting, flySetting, settings.keepInventory(), settings.keepExp(), settings.voidTeleport(), settings.period());

                saveSettings();
            }

            default -> throw new RuntimeException("Unknown config version in settings.yml.");
        }
    }

    /**
     * Save the plugin's settings.
     */
    private void saveSettings() {
        if(settings == null) return;
        Path path = Path.of(newPlayerPerks.getDataFolder() + File.separator + "settings.yml");

        YamlConfigurationLoader loader = createLoader(path);
        CommentedConfigurationNode node = loader.createNode();
        try {
            node.set(Settings.class, settings);

            loader.save(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create the {@link YamlConfigurationLoader} for the path provided.
     * @param path The {@link Path}.
     * @return The {@link YamlConfigurationLoader}.
     */
    protected @NonNull YamlConfigurationLoader createLoader(@NonNull Path path) {
        return YamlConfigurationLoader.builder()
                .path(path)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(4)
                .build();
    }
}
