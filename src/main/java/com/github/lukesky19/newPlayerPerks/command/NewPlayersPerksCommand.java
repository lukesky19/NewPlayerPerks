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
package com.github.lukesky19.newPlayerPerks.command;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.command.arguments.AddCommand;
import com.github.lukesky19.newPlayerPerks.command.arguments.ReloadCommand;
import com.github.lukesky19.newPlayerPerks.command.arguments.RemoveCommand;
import com.github.lukesky19.newPlayerPerks.configuration.locale.LocaleManager;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.PlayerDataManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;

/**
 * This class handles the creation of the newplayerperks command.
 */
public class NewPlayersPerksCommand {
    private final @NotNull NewPlayerPerks newPlayerPerks;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PerksManager perksManager;
    private final @NotNull PlayerDataManager playerDataManager;

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param perksManager A {@link PerksManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public NewPlayersPerksCommand(
            @NotNull NewPlayerPerks newPlayerPerks,
            @NotNull LocaleManager localeManager,
            @NotNull PerksManager perksManager,
            @NotNull PlayerDataManager playerDataManager) {
        this.newPlayerPerks = newPlayerPerks;
        this.localeManager = localeManager;
        this.perksManager = perksManager;
        this.playerDataManager = playerDataManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the newplayerperks command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the newplayerperks command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("newplayerperks")
                .requires(ctx -> ctx.getSender().hasPermission("newplayerperks.commands.newplayerperks"));

        ReloadCommand reloadCommand = new ReloadCommand(newPlayerPerks, localeManager);
        AddCommand addCommand = new AddCommand(localeManager, perksManager, playerDataManager);
        RemoveCommand removeCommand = new RemoveCommand(localeManager, perksManager, playerDataManager);

        builder.then(reloadCommand.createCommand());
        builder.then(addCommand.createCommand());
        builder.then(removeCommand.createCommand());

        return builder.build();
    }
}
