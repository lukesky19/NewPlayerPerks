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
import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.newPlayerPerks.locale.Locale;
import com.github.lukesky19.newPlayerPerks.locale.LocaleManager;
import com.github.lukesky19.newPlayerPerks.manager.PlayerDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * This class is used to create the time command argument.
 */
public class TimeCommand {
    private final @NotNull ComponentLogger logger;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PlayerDataManager playerDataManager;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public TimeCommand(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull LocaleManager localeManager,
            @NotNull PlayerDataManager playerDataManager) {
        this.logger = newPlayerPerks.getComponentLogger();
        this.localeManager = localeManager;
        this.playerDataManager = playerDataManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the enable command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the enable command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("time")
                .requires(ctx -> ctx.getSender().hasPermission("newplayerperks.commands.newplayerperks.time") && ctx.getSender() instanceof Player)
                .then(Commands.argument("player", ArgumentTypes.player())
                    .requires(ctx -> ctx.getSender().hasPermission("newplayerperks.commands.newplayerperks.time.other"))
                    .executes(ctx -> {
                        Locale locale = localeManager.getLocale();
                        Player commandSender = (Player) ctx.getSource().getSender();
                        PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                        Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                        UUID targetPlayerId = targetPlayer.getUniqueId();

                        PlayerData playerData = playerDataManager.getPlayerData(targetPlayerId);
                        if(playerData == null) {
                            commandSender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.playerDataError()));
                            logger.error(AdventureUtil.deserialize("Unable to display perk time for player " + targetPlayer.getName() + " due to no player data found for that player."));
                            return 0;
                        }

                        List<TagResolver.Single> placeholders = List.of(
                                Placeholder.parsed("player_name", targetPlayer.getName()),
                                Placeholder.parsed("time", localeManager.getTimeMessage(playerData.getPerkTime())));

                        commandSender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.playerPerkTime(), placeholders));

                        return 1;
                    })
                )
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();
                    PlayerData playerData = playerDataManager.getPlayerData(uuid);
                    if(playerData == null) {
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.playerDataError()));
                        logger.error(AdventureUtil.deserialize("Unable to display perk time for player " + player.getName() + " due to no player data found for that player."));
                        return 0;
                    }

                    List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("time", localeManager.getTimeMessage(playerData.getPerkTime())));

                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.perkTime(), placeholders));

                    return 1;
                }).build();
    }
}