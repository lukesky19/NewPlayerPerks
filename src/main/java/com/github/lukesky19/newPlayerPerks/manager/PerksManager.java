package com.github.lukesky19.newPlayerPerks.manager;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.configuration.locale.LocaleManager;
import com.github.lukesky19.newPlayerPerks.configuration.settings.Settings;
import com.github.lukesky19.newPlayerPerks.configuration.settings.SettingsManager;
import com.github.lukesky19.skylib.format.FormatUtil;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PerksManager {
    private final NewPlayerPerks newPlayerPerks;
    private final SettingsManager settingsManager;
    private final LocaleManager localeManager;
    private final ConcurrentHashMap<UUID, Long> players = new ConcurrentHashMap<>();

    public PerksManager(NewPlayerPerks newPlayerPerks, SettingsManager settingsManager, LocaleManager localeManager) {
        this.newPlayerPerks = newPlayerPerks;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
    }

    public void reload() {
        for(Player player : newPlayerPerks.getServer().getOnlinePlayers()) {
            if(isPlayerNew(player)) {
                if (player.isOnline() && player.isConnected()) {
                    UUID uuid = player.getUniqueId();
                    removePerks(player, uuid);
                    applyPerks(player, uuid);
                }
            }
        }
    }

    public boolean isPlayerNew(Player player) {
        return players.containsKey(player.getUniqueId());
    }

    public void addplayer(Player player, UUID uuid, long time) {
        players.put(uuid, time);
        applyPerks(player, uuid);
    }

    public void removePlayer(UUID uuid) {
        if(players.containsKey(uuid)) {
            Player player = newPlayerPerks.getServer().getPlayer(uuid);
            if(player != null && player.isOnline() && player.isConnected()) {
                removePerks(player, uuid);

                for(String msg : localeManager.getLocale().perksExpireMessages()) {
                    player.sendMessage(FormatUtil.format(player, localeManager.getLocale().prefix() + msg));
                }
            }
        }

        players.remove(uuid);
    }

    public void handleLogout(Player player, UUID uuid) {
        if(players.containsKey(uuid)) {
            removePerks(player, uuid);
            players.remove(uuid);
        }
    }

    public void checkPerksTask() {
        newPlayerPerks.getServer().getScheduler().runTaskTimer(newPlayerPerks, this::checkPerks, 1L, 20L * 600L);
    }

    private void checkPerks() {
        for(Map.Entry<UUID, Long> uuidLongEntry : players.entrySet()) {
            UUID uuid = uuidLongEntry.getKey();
            long time = uuidLongEntry.getValue();
            long expire = time + settingsManager.getPeriod();

            if(System.currentTimeMillis() > expire) {
                removePlayer(uuid);
            }
        }
    }

    private void applyPerks(Player player, UUID uuid) {
        // Get Plugin Settings
        Settings settings = settingsManager.getSettings();

        // Get LuckPerms User
        UserManager userManager = newPlayerPerks.getLuckPermsApi().getUserManager();
        User user = userManager.getUser(uuid);
        if(user != null) {
            // Invulnerable
            if(settings.invulnerable()) {
                player.setInvulnerable(true);
            }

            // Fly
            if(settings.fly()) {
                PermissionNode eFly = PermissionNode.builder("essentials.fly").value(true).build();
                PermissionNode iFly = PermissionNode.builder("bskyblock.island.fly").value(true).build();
                Objects.requireNonNull(user).data().add(eFly);
                user.data().add(iFly);
                player.setAllowFlight(true);
                player.setFlying(true);
            }

            // NOTE: Keep Inventory and Keep Exp is checked on Death.

            // Void Teleport
            if(settings.voidTeleport()) {
                PermissionNode voidTele = PermissionNode.builder("bskyblock.voidteleport").value(true).build();
                user.data().add(voidTele);
            }

            // Save modified User
            userManager.saveUser(user);
        }
    }

    private void removePerks(Player player, UUID uuid) {
        // Get LuckPerms User
        UserManager userManager = newPlayerPerks.getLuckPermsApi().getUserManager();
        User user = userManager.getUser(uuid);

        // Give Fly
        PermissionNode eFly = PermissionNode.builder("essentials.fly").build();
        PermissionNode iFly = PermissionNode.builder("bskyblock.island.fly").build();
        Objects.requireNonNull(user).data().remove(eFly);
        user.data().remove(iFly);
        player.setAllowFlight(false);
        player.setFlying(false);

        // Give Void Teleport
        PermissionNode voidTele = PermissionNode.builder("bskyblock.voidteleport").build();
        user.data().remove(voidTele);

        // Invulnerable
        player.setInvulnerable(false);

        // Save modified User
        userManager.saveUser(user);
    }
}
