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
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.config.LocaleManager;
import com.github.lukesky19.newPlayerPerks.util.PerksResult;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * This class is used to create the remove command argument.
 */
public class RemoveCommand {
    private final @NotNull ComponentLogger logger;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PerksManager perksManager;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param perksManager A {@link PerksManager} instance.
     */
    public RemoveCommand(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull LocaleManager localeManager,
            @NotNull PerksManager perksManager) {
        this.logger = newPlayerPerks.getComponentLogger();
        this.localeManager = localeManager;
        this.perksManager = perksManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the remove command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the remove command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("remove")
            .requires(ctx -> ctx.getSender().hasPermission("newplayerperks.commands.newplayerperks.remove"))
            .then(Commands.argument("player", ArgumentTypes.player())
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    CommandSender sender = ctx.getSource().getSender();
                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                    Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                    UUID targetPlayerId = targetPlayer.getUniqueId();

                    List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("player_name", targetPlayer.getName()));

                    PerksResult perksResult = perksManager.removePerks(targetPlayer, targetPlayerId);
                    switch(perksResult) {
                        case SUCCESS -> {
                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtil.serialize(targetPlayer, locale.prefix() + locale.removedPerks(), placeholders));
                            } else {
                                logger.info(AdventureUtil.serialize(targetPlayer, locale.removedPerks(), placeholders));
                            }

                            for(String msg : locale.perksRemovedMessages()) {
                                targetPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + msg));
                            }
                        }

                        case EXPIRED -> {
                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtil.serialize(targetPlayer, locale.prefix() + locale.expiredError()));
                            } else {
                                logger.info(AdventureUtil.serialize(targetPlayer, locale.expiredError()));
                            }
                        }

                        case SETTINGS_ERROR -> {
                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtil.serialize(targetPlayer, locale.prefix() + locale.settingsError()));
                            } else {
                                logger.info(AdventureUtil.serialize(targetPlayer, locale.settingsError()));
                            }
                        }

                        case NO_PLAYER_DATA -> {
                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtil.serialize(targetPlayer, locale.prefix() + locale.playerDataError()));
                            } else {
                                logger.info(AdventureUtil.serialize(targetPlayer, locale.playerDataError()));
                            }
                        }

                        case USER_ERROR -> {
                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtil.serialize(targetPlayer, locale.prefix() + locale.userError()));
                            } else {
                                logger.info(AdventureUtil.serialize(targetPlayer, locale.userError()));
                            }
                        }
                    }

                    return 1;
                })).build();
    }
}
