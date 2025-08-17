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
package com.github.lukesky19.newPlayerPerks.manager.config;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.data.Locale;
import com.github.lukesky19.newPlayerPerks.data.Settings;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.api.time.Time;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
            "1.1.0.0",
            "<aqua><bold>NewPlayerPerks</bold></aqua><gray> ▪ </gray>",
            "<green>Configuration files have been reloaded.</green>",
            "<green>Perks have been successfully added to this player.</green>",
            "<green>Perks have been successfully removed from this player.</green>",
            "<red>Unable to process your request due to no player data found.</red>",
            "<red>Unable to process your request due to no LuckPerms' User found.</red>",
            "<red>Unable to process your request due to invalid plugin settings.</red>",
            "<red>Unable to process your request due to your perks having already expired.</red>",
            "<red>Unable to enable perks because they have already expired.</red>",
            "<red>Unable to disable perks because they have already expired.</red>",
            "<red>Your perks have been disabled due to a plugin reload. They will be re-enabled after the reload is complete.</red>",
            List.of(
                    "<green>For the next 6 hours, you are now invulnerable, have keep inventory, and will be teleported to your island if you fall into the void.</green>",
                    "<green>Use this time to get a jump start on your island and get to know the server.</green>"),
            List.of(
                    "<green>Your perks have been enabled. Your perks will expire at <expire_time>. Remaining time: <remaining_time></green>"),
            List.of(
                    "<red>Your perks have been removed. You are no longer invulnerable, don't have keep inventory, and won't be teleported to your island if you fall into the void.</red>"),
            List.of(
                    "<green>Your perks have been disabled. You can re-enable them using /perks enable as long as they haven't expired.</green>",
                    "<green>Your perks will expire at <expire_time>. Remaining time: <remaining_time></green>"),
            List.of(
                    "<red>Your invulnerability, keep inventory, void teleport perks have expired!</red>",
                    "<red>You can now take damage, die, and you won't be teleported to your island if you fall into the void!</red>"),
            new Locale.TimeMessage(
                    "",
                    "<yellow><years></yellow> year(s)",
                    "<yellow><months></yellow> month(s)",
                    "<yellow><weeks></yellow> week(s)",
                    "<yellow><days></yellow> day(s)",
                    "<yellow><hours></yellow> hour(s)",
                    "<yellow><minutes></yellow> minute(s)",
                    "<yellow><seconds></yellow> second(s)",
                    "."));
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
            return;
        }

        migrateLocale();
    }

    private void saveLocale() {
        if(locale == null) return;
        ComponentLogger logger = newPlayerPerks.getComponentLogger();

        Settings settings = settingsManager.getSettings();
        if(settings == null) {
            logger.error(AdventureUtil.serialize("Unable to save the plugin's locale due to invalid plugin settings. The default locale will be used."));
            return;
        }
        if(settings.locale() == null) {
            logger.error(AdventureUtil.serialize("Unable to save the plugin's locale due to a locale not being configured in settings.yml. The default locale will be used."));
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
        CommentedConfigurationNode node = loader.createNode();

        try {
            node.set(Locale.class, locale);

            loader.save(node);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.serialize("Failed to save the locale configuration. Error: " + e.getMessage()));
        }
    }

    private void migrateLocale() {
        if(locale == null) return;
        ComponentLogger logger = newPlayerPerks.getComponentLogger();

        switch(locale.configVersion()) {
            case "1.1.0.0" -> {
                // Current version, do nothing
            }

            // 1.0.0.0
            case null -> {
                locale = new Locale(
                        "1.1.0.0",
                        locale.prefix(),
                        locale.reload(),
                        "<green>Perks have been successfully added to player <player_name></green>",
                        "<green>Perks have been successfully removed from player <player_name>.</green>",
                        "<red>Unable to process your request due to no player data found.</red>",
                        "<red>Unable to process your request due to no LuckPerms' User found.</red>",
                        "<red>Unable to process your request due to invalid plugin settings.</red>",
                        "<red>Unable to process your request due to your perks having already expired.</red>",
                        "<red>Unable to enable perks because they have already expired.</red>",
                        "<red>Unable to disable perks because they have already expired.</red>",
                        "<red>Your perks have been disabled due to a plugin reload. They will be re-enabled after the reload is complete.</red>",
                        List.of(
                                "<green>For the next 6 hours, you are now invulnerable, have keep inventory, and will be teleported to your island if you fall into the void.</green>",
                                "<green>Use this time to get a jump start on your island and get to know the server.</green>"),
                        List.of(
                                "<green>Your perks have been enabled. Your perks will expire at <expire_time>. Remaining time: <remaining_time></green>"),
                        List.of(
                                "<red>Your perks have been removed. You are no longer invulnerable, don't have keep inventory, and won't be teleported to your island if you fall into the void.</red>"),
                        List.of(
                                "<green>Your perks have been disabled. You can re-enable them using /perks enable as long as they haven't expired.</green>",
                                "<green>Your perks will expire at <expire_time>. Remaining time: <remaining_time></green>"),
                        List.of(
                                "<red>Your invulnerability, keep inventory, void teleport perks have expired!</red>",
                                "<red>You can now take damage, die, and you won't be teleported to your island if you fall into the void!</red>"),
                        new Locale.TimeMessage(
                                "",
                                "<yellow><years></yellow> year(s)",
                                "<yellow><months></yellow> month(s)",
                                "<yellow><weeks></yellow> week(s)",
                                "<yellow><days></yellow> day(s)",
                                "<yellow><hours></yellow> hour(s)",
                                "<yellow><minutes></yellow> minute(s)",
                                "<yellow><seconds></yellow> second(s)",
                                "."));

                saveLocale();
            }

            default -> logger.error(AdventureUtil.serialize("Unable to migrate locale configuration due to an unrecognized config version."));
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

    /**
     * Gets the time message to display.
     * @param timeMilliseconds The time in milliseconds.
     * @return A String containing the time message.
     */
    @NotNull
    public String getTimeMessage(long timeMilliseconds) {
        Locale locale = this.getLocale();
        Time timeRecord = TimeUtil.millisToTime(timeMilliseconds);

        List<TagResolver.Single> placeholders = List.of(
                Placeholder.parsed("years", String.valueOf(timeRecord.years())),
                Placeholder.parsed("months", String.valueOf(timeRecord.months())),
                Placeholder.parsed("weeks", String.valueOf(timeRecord.weeks())),
                Placeholder.parsed("days", String.valueOf(timeRecord.days())),
                Placeholder.parsed("hours", String.valueOf(timeRecord.hours())),
                Placeholder.parsed("minutes", String.valueOf(timeRecord.minutes())),
                Placeholder.parsed("seconds", String.valueOf(timeRecord.seconds())));

        StringBuilder stringBuilder = getStringBuilder(locale, timeRecord);

        return MiniMessage.miniMessage().serialize(AdventureUtil.serialize(stringBuilder.toString(), placeholders));
    }

    /**
     * Builds the string by populating any non-zero individual time units.
     * @param locale The plugin's locale
     * @param timeRecord The record containing the individual time units to display.
     * @return A populated StringBuilder. May be empty if all time units were 0 and no suffix was configured.
     */
    private @NotNull StringBuilder getStringBuilder(Locale locale, Time timeRecord) {
        Locale.TimeMessage timeMessage = locale.timeMessage();
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(timeMessage.prefix());

        boolean isFirstUnit = true;

        if(timeRecord.years() > 0) {
            stringBuilder.append(timeMessage.years());
            isFirstUnit = false;
        }

        if (timeRecord.months() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.months());
            isFirstUnit = false;
        }

        if (timeRecord.weeks() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.weeks());
            isFirstUnit = false;
        }

        if (timeRecord.days() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.days());
            isFirstUnit = false;
        }

        if (timeRecord.hours() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.hours());
            isFirstUnit = false;
        }

        if (timeRecord.minutes() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.minutes());
            isFirstUnit = false;
        }

        if (timeRecord.seconds() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.seconds());
            isFirstUnit = false;
        }

        if(isFirstUnit) {
            stringBuilder.append(timeMessage.seconds());
        }

        stringBuilder.append(timeMessage.suffix());
        return stringBuilder;
    }
}
