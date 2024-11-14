package com.github.lukesky19.newPlayerPerks.listener;

import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.configuration.locale.LocaleManager;
import com.github.lukesky19.newPlayerPerks.configuration.player.PlayerManager;
import com.github.lukesky19.newPlayerPerks.configuration.player.PlayerSettings;
import com.github.lukesky19.newPlayerPerks.configuration.settings.SettingsManager;
import com.github.lukesky19.skylib.format.FormatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;
import java.util.UUID;

public class JoinListener implements Listener {
    private final SettingsManager settingsManager;
    private final LocaleManager localeManager;
    private final PlayerManager playerManager;
    private final PerksManager perksManager;

    public JoinListener(SettingsManager settingsManager, LocaleManager localeManager, PlayerManager playerManager, PerksManager perksManager) {
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.playerManager = playerManager;
        this.perksManager = perksManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        playerManager.createPlayerSettings(player);

        if(!player.hasPlayedBefore()) {
            for(String msg : localeManager.getLocale().newPlayerMessages()) {
                player.sendMessage(FormatUtil.format(player, localeManager.getLocale().prefix() + msg));
            }
        }

        PlayerSettings playerSettings = playerManager.getPlayerSettings(player);
        long time = Objects.requireNonNull(playerSettings).joinTime();

        if(System.currentTimeMillis() < (time + settingsManager.getPeriod())) {
            perksManager.addplayer(player, uuid, playerSettings.joinTime());
        }
    }
}
