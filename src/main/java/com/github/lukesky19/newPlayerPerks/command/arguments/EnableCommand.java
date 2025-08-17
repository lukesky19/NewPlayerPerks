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
package com.github.lukesky19.newPlayerPerks.command.arguments;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.data.Locale;
import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.PlayerDataManager;
import com.github.lukesky19.newPlayerPerks.manager.config.LocaleManager;
import com.github.lukesky19.newPlayerPerks.manager.config.SettingsManager;
import com.github.lukesky19.newPlayerPerks.util.PerksResult;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * This class is used to create the enable command argument.
 */
public class EnableCommand {
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull PerksManager perksManager;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param perksManager A {@link PerksManager} instance.
     */
    public EnableCommand(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull PerksManager perksManager) {
        this.logger = newPlayerPerks.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.playerDataManager = playerDataManager;
        this.perksManager = perksManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the enable command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the enable command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("enable")
            .requires(ctx -> ctx.getSender().hasPermission("newplayerperks.commands.newplayerperks.enable") && ctx.getSender() instanceof Player)
            .executes(ctx -> {
                Locale locale = localeManager.getLocale();
                Player player = (Player) ctx.getSource().getSender();
                UUID uuid = player.getUniqueId();
                if(settingsManager.getPeriod() == null) {
                    logger.error(AdventureUtil.serialize("Unable to enable perks for player " + player.getName() + " due to an invalid period in settings.yml."));
                    return 0;
                }
                PlayerData playerData = playerDataManager.getPlayerData(uuid);
                if(playerData == null) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.playerDataError()));
                    logger.error(AdventureUtil.serialize("Unable to enable perks for player " + player.getName() + " due to no player data found for that player."));
                    return 0;
                }

                PerksResult perksResult = perksManager.enablePerks(player, uuid);
                switch(perksResult) {
                    case SUCCESS -> {
                        List<TagResolver.Single> placeholders = List.of(
                                Placeholder.parsed("expire_time", TimeUtil.millisToTimeStamp((playerData.getJoinTime() + settingsManager.getPeriod()), ZoneId.of("America/New_York"), "MM-dd-yyyy HH:mm:ss z")),
                                Placeholder.parsed("remaining_time", localeManager.getTimeMessage((playerData.getJoinTime() + settingsManager.getPeriod()) - System.currentTimeMillis())));

                        for(String msg : locale.perksEnabledMessages()) {
                            player.sendMessage(AdventureUtil.serialize(locale.prefix() + msg, placeholders));
                        }
                    }

                    case EXPIRED -> player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.enablePerksExpired()));

                    case SETTINGS_ERROR -> {
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.settingsError()));
                        logger.error(AdventureUtil.serialize("Unable to enable perks for player " + player.getName() + " due to invalid plugin settings."));
                    }

                    case NO_PLAYER_DATA -> {
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.playerDataError()));
                        logger.error(AdventureUtil.serialize("Unable to enable perks for player " + player.getName() + " due to no player data found for that player."));
                    }

                    case USER_ERROR -> {
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.userError()));
                        logger.error(AdventureUtil.serialize("Unable to enable perks for player " + player.getName() + " due to no LuckPerms' User found for that player."));
                    }
                }

                return 1;
            }).build();
    }
}
