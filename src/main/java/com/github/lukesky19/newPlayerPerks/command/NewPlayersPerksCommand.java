package com.github.lukesky19.newPlayerPerks.command;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.configuration.locale.Locale;
import com.github.lukesky19.newPlayerPerks.configuration.locale.LocaleManager;
import com.github.lukesky19.newPlayerPerks.configuration.player.PlayerManager;
import com.github.lukesky19.newPlayerPerks.configuration.player.PlayerSettings;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.skylib.format.FormatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NewPlayersPerksCommand implements CommandExecutor, TabExecutor {
    private final NewPlayerPerks newPlayerPerks;
    private final LocaleManager localeManager;
    private final PerksManager perksManager;
    private final PlayerManager playerManager;

    public NewPlayersPerksCommand(NewPlayerPerks newPlayerPerks, LocaleManager localeManager, PerksManager perksManager, PlayerManager playerManager) {
        this.newPlayerPerks = newPlayerPerks;
        this.localeManager = localeManager;
        this.perksManager = perksManager;
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Locale locale = localeManager.getLocale();

        if(sender instanceof Player player) {
            if(player.hasPermission("newplayerperks.command.newplayerperks")) {
                switch(args.length) {
                    case 1 -> {
                        if (args[0].equalsIgnoreCase("reload")) {
                            if (player.hasPermission("newplayerperks.command.newplayerperks.reload")) {
                                newPlayerPerks.reload();
                                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.reload()));
                                return true;
                            } else {
                                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                                return false;
                            }
                        } else {
                            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.unknownArgument()));
                            return false;
                        }
                    }

                    case 2 -> {
                        switch(args[0].toLowerCase()) {
                            case "add" -> {
                                if (player.hasPermission("newplayerperks.command.newplayerperks.add")) {
                                    Player target = newPlayerPerks.getServer().getPlayer(args[1]);
                                    if(target != null && target.isOnline() && target.isConnected()) {
                                        long time = System.currentTimeMillis();
                                        perksManager.addplayer(target, target.getUniqueId(), time);
                                        playerManager.savePlayerSettings(target, new PlayerSettings(time));

                                        player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.addedPerks()));

                                        return true;
                                    } else {
                                        player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.invalidPlayer()));
                                        return false;
                                    }
                                } else {
                                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                                    return false;
                                }
                            }

                            case "remove" -> {
                                if (player.hasPermission("newplayerperks.command.newplayerperks.remove")) {
                                    Player target = newPlayerPerks.getServer().getPlayer(args[1]);
                                    if(target != null && target.isOnline() && target.isConnected()) {
                                        perksManager.removePlayer(target.getUniqueId());
                                        playerManager.savePlayerSettings(target, new PlayerSettings(0));

                                        player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.removedPerks()));

                                        return true;
                                    } else {
                                        player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.invalidPlayer()));
                                        return false;
                                    }
                                } else {
                                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                                    return false;
                                }
                            }

                            default -> {
                                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.unknownArgument()));
                                return false;
                            }
                        }
                    }

                    default -> {
                        player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.unknownArgument()));
                        return false;
                    }
                }
            } else {
                player.sendMessage(FormatUtil.format(player,locale.prefix() + locale.noPermission()));
                return false;
            }
        } else {
            switch(args.length) {
                case 1 -> {
                    if(args[0].equalsIgnoreCase("reload")) {
                        newPlayerPerks.reload();
                        newPlayerPerks.getComponentLogger().info(FormatUtil.format(locale.reload()));
                        return true;
                    } else {
                        newPlayerPerks.getComponentLogger().info(FormatUtil.format(locale.unknownArgument()));
                        return false;
                    }
                }

                case 2 -> {
                    switch(args[0].toLowerCase()) {
                        case "add" -> {
                            Player target = newPlayerPerks.getServer().getPlayer(args[1]);
                            if(target != null && target.isOnline() && target.isConnected()) {
                                long time = System.currentTimeMillis();
                                perksManager.addplayer(target, target.getUniqueId(), time);
                                playerManager.savePlayerSettings(target, new PlayerSettings(time));

                                newPlayerPerks.getComponentLogger().info(FormatUtil.format(locale.addedPerks()));

                                return true;
                            } else {
                                newPlayerPerks.getComponentLogger().info(FormatUtil.format(locale.invalidPlayer()));
                                return false;
                            }
                        }

                        case "remove" -> {
                            Player target = newPlayerPerks.getServer().getPlayer(args[1]);
                            if(target != null && target.isOnline() && target.isConnected()) {
                                perksManager.removePlayer(target.getUniqueId());

                                newPlayerPerks.getComponentLogger().info(FormatUtil.format(locale.removedPerks()));

                                return true;
                            } else {
                                newPlayerPerks.getComponentLogger().info(FormatUtil.format(locale.invalidPlayer()));
                                return false;
                            }
                        }

                        default -> {
                            newPlayerPerks.getComponentLogger().info(FormatUtil.format(locale.unknownArgument()));
                            return false;
                        }
                    }
                }

                default -> {
                    newPlayerPerks.getComponentLogger().info(FormatUtil.format(locale.unknownArgument()));
                    return false;
                }
            }


        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> arguments = new ArrayList<>();

        if(sender instanceof Player player) {
            switch(args.length) {
                case 1 -> {
                    if (player.hasPermission("newplayerperks.command.newplayerperks.reload")) {
                        arguments.add("reload");
                    }
                    if (player.hasPermission("newplayerperks.command.newplayerperks.add")) {
                        arguments.add("add");
                    }
                    if (player.hasPermission("newplayerperks.command.newplayerperks.remove")) {
                        arguments.add("remove");
                    }
                }

                case 2 -> {
                    if(player.hasPermission("newplayerperks.command.newplayerperks.add") || player.hasPermission("newplayerperks.command.newplayerperks.remove")) {
                        for(Player p : newPlayerPerks.getServer().getOnlinePlayers()) {
                            arguments.add(p.getName());
                        }
                    }
                }
            }
        } else {
            switch(args.length) {
                case 1 -> {
                    arguments.add("reload");
                    arguments.add("add");
                    arguments.add("remove");
                }

                case 2 -> {
                    for(Player p : newPlayerPerks.getServer().getOnlinePlayers()) {
                        arguments.add(p.getName());
                    }
                }
            }
        }

        return arguments;
    }
}
