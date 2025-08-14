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
import com.github.lukesky19.newPlayerPerks.manager.LocaleManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used to create the reload command argument.
 */
public class ReloadCommand {
    private final @NotNull NewPlayerPerks newPlayerPerks;
    private final @NotNull LocaleManager localeManager;
    /**
     * Default Constructor.
     * You should use {@link #ReloadCommand(NewPlayerPerks, LocaleManager)} instead.
     * @deprecated You should use {@link #ReloadCommand(NewPlayerPerks, LocaleManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public ReloadCommand() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param newPlayerPerks A {@link NewPlayerPerks} instance.
     * @param localeManager A {@link LocaleManager} instance.
     */
    public ReloadCommand(@NotNull NewPlayerPerks newPlayerPerks, @NotNull LocaleManager localeManager) {
        this.newPlayerPerks = newPlayerPerks;
        this.localeManager = localeManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the reload command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the reload command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("reload")
                .requires(ctx -> ctx.getSender().hasPermission("newplayerperks.commands.newplayerperks.reload"))
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();

                    newPlayerPerks.reload();

                    ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(locale.prefix() + locale.reload()));

                    return 1;
                });

        return builder.build();
    }
}
