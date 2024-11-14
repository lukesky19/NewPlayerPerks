package com.github.lukesky19.newPlayerPerks;

import com.github.lukesky19.newPlayerPerks.command.NewPlayersPerksCommand;
import com.github.lukesky19.newPlayerPerks.configuration.locale.LocaleManager;
import com.github.lukesky19.newPlayerPerks.configuration.player.PlayerManager;
import com.github.lukesky19.newPlayerPerks.configuration.settings.SettingsManager;
import com.github.lukesky19.newPlayerPerks.listener.DamageListener;
import com.github.lukesky19.newPlayerPerks.listener.DeathListener;
import com.github.lukesky19.newPlayerPerks.listener.JoinListener;
import com.github.lukesky19.newPlayerPerks.listener.QuitListener;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class NewPlayerPerks extends JavaPlugin {
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private PerksManager perksManager;
    private LuckPerms luckPermsApi;

    public LuckPerms getLuckPermsApi() {
        return luckPermsApi;
    }

    @Override
    public void onEnable() {
        loadLuckPerms();

        settingsManager = new SettingsManager(this);
        PlayerManager playerManager = new PlayerManager(this);
        localeManager = new LocaleManager(this, settingsManager);
        perksManager = new PerksManager(this, settingsManager, localeManager);
        perksManager.checkPerksTask();

        this.getServer().getPluginManager().registerEvents(new JoinListener(settingsManager, localeManager, playerManager, perksManager), this);
        this.getServer().getPluginManager().registerEvents(new QuitListener(perksManager), this);
        this.getServer().getPluginManager().registerEvents(new DamageListener(perksManager), this);
        this.getServer().getPluginManager().registerEvents(new DeathListener(perksManager, settingsManager), this);

        NewPlayersPerksCommand newPlayersCommandsCommand = new NewPlayersPerksCommand(this, localeManager, perksManager, playerManager);
        Objects.requireNonNull(this.getServer().getPluginCommand("newplayerperks")).setExecutor(newPlayersCommandsCommand);
        Objects.requireNonNull(this.getServer().getPluginCommand("newplayerperks")).setTabCompleter(newPlayersCommandsCommand);

        reload();
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
    }

    public void reload() {
        settingsManager.reload();
        localeManager.reload();
        perksManager.reload();
    }

    private void loadLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPermsApi = provider.getProvider();
        } else {
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }
}
