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

import com.github.lukesky19.newPlayerPerks.data.Locale;
import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.newPlayerPerks.manager.LocaleManager;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.PlayerDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class is used to create the add command argument.
 */
public class AddCommand {
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PerksManager perksManager;
    private final @NotNull PlayerDataManager playerDataManager;

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     * @param perksManager A {@link PerksManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public AddCommand(
            @NotNull LocaleManager localeManager,
            @NotNull PerksManager perksManager,
            @NotNull PlayerDataManager playerDataManager) {
        this.localeManager = localeManager;
        this.perksManager = perksManager;
        this.playerDataManager = playerDataManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the add command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the add command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("add")
                .requires(ctx -> ctx.getSender().hasPermission("newplayerperks.commands.newplayerperks.add"));

        builder.then(Commands.argument("player", ArgumentTypes.player())
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                    Player targetPlayer = targetResolver.resolve(ctx.getSource()).getFirst();
                    UUID targetPlayerId = targetPlayer.getUniqueId();
                    Locale locale = localeManager.getLocale();

                    long time = System.currentTimeMillis();
                    playerDataManager.savePlayerData(targetPlayerId, new PlayerData(time));
                    perksManager.applyPerks(targetPlayer, targetPlayerId);

                    if(sender instanceof Player) {
                        sender.sendMessage(AdventureUtil.serialize(targetPlayer, locale.prefix() + locale.addedPerks()));
                    } else {
                        sender.sendMessage(AdventureUtil.serialize(targetPlayer, locale.addedPerks()));
                    }

                    return 1;
                }));

        return builder.build();
    }
}
