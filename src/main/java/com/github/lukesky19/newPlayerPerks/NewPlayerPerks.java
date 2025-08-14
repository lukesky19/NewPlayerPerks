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
package com.github.lukesky19.newPlayerPerks;

import com.github.lukesky19.newPlayerPerks.command.NewPlayersPerksCommand;
import com.github.lukesky19.newPlayerPerks.data.PlayerData;
import com.github.lukesky19.newPlayerPerks.listener.DamageListener;
import com.github.lukesky19.newPlayerPerks.listener.DeathListener;
import com.github.lukesky19.newPlayerPerks.listener.JoinListener;
import com.github.lukesky19.newPlayerPerks.listener.QuitListener;
import com.github.lukesky19.newPlayerPerks.manager.LocaleManager;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.PlayerDataManager;
import com.github.lukesky19.newPlayerPerks.manager.SettingsManager;
import com.github.lukesky19.newPlayerPerks.manager.database.ConnectionManager;
import com.github.lukesky19.newPlayerPerks.manager.database.DatabaseManager;
import com.github.lukesky19.newPlayerPerks.manager.database.QueueManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

/**
 * The main plugin class
 */
public final class NewPlayerPerks extends JavaPlugin {
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private PerksManager perksManager;

    private LuckPerms luckPermsAPI;

    /**
     * Default Constructor.
     */
    public NewPlayerPerks() {}

    /**
     * Get the {@link LuckPerms} api.
     * @return The {@link LuckPerms} api.
     */
    public LuckPerms getLuckPermsAPI() {
        return luckPermsAPI;
    }

    /**
     * The method ran on plugin startup.
     */
    @Override
    public void onEnable() {
        if(!checkSkyLibVersion()) return;
        if(!setupLuckPermsAPI()) return;

        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, settingsManager);

        ConnectionManager connectionManager = new ConnectionManager(this);
        QueueManager queueManager = new QueueManager(connectionManager);
        databaseManager = new DatabaseManager(connectionManager, queueManager);

        playerDataManager = new PlayerDataManager(this, settingsManager, databaseManager);
        perksManager = new PerksManager(this, settingsManager, localeManager, playerDataManager);

        perksManager.startCheckPerksTask();

        this.getServer().getPluginManager().registerEvents(new JoinListener(this, settingsManager, localeManager, playerDataManager, perksManager), this);
        this.getServer().getPluginManager().registerEvents(new QuitListener(this, settingsManager, playerDataManager, perksManager), this);
        this.getServer().getPluginManager().registerEvents(new DamageListener(perksManager), this);
        this.getServer().getPluginManager().registerEvents(new DeathListener(settingsManager, perksManager), this);

        NewPlayersPerksCommand newPlayersCommandCommand = new NewPlayersPerksCommand(this, localeManager, perksManager, playerDataManager);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands ->
                        commands.registrar().register(newPlayersCommandCommand.createCommand(),
                                "Command to manage and use the NewPlayerPerks plugin.",
                                List.of("npp")));

        reload();
    }

    /**
     * The method ran on plugin disable.
     */
    @Override
    public void onDisable() {
        if(perksManager != null) perksManager.stopCheckPerksTask();

        if(databaseManager != null) databaseManager.handlePluginDisable();
    }

    /**
     * Reloads all plugin data.
     */
    public void reload() {
        settingsManager.reload();
        localeManager.reload();

        this.getServer().getOnlinePlayers().forEach(player -> {
            UUID uuid = player.getUniqueId();
            PlayerData playerData = playerDataManager.getPlayerData(uuid);

            if(playerData != null) {
                perksManager.removePerks(player, uuid, false);
            }
        });

        playerDataManager.migrateLegacyPlayerData();

        playerDataManager.reload().thenAccept(v -> {
            if(settingsManager.getPeriod() == null) {
                this.getComponentLogger().error(AdventureUtil.serialize("Unable to check if perks should be applied due to an invalid period in settings.yml."));
                return;
            }

            this.getServer().getOnlinePlayers().forEach(player -> {
                UUID uuid = player.getUniqueId();
                PlayerData playerData = playerDataManager.getPlayerData(uuid);

                if(playerData != null) {
                    if(System.currentTimeMillis() < (playerData.joinTime() + settingsManager.getPeriod())) {
                        perksManager.applyPerks(player, uuid);
                    } else {
                        playerDataManager.unloadPlayerData(uuid);
                    }
                }
            });
        });
    }

    /**
     * Retrieves the {@link LuckPerms} api.
     * @return true if successful, otherwise false.
     */
    private boolean setupLuckPermsAPI() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if(provider != null) {
            luckPermsAPI = provider.getProvider();
            return true;
        } else {
            this.getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    /**
     * Checks if the Server has the proper SkyLib version.
     * @return true if it does, false if not.
     */
    private boolean checkSkyLibVersion() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        Plugin skyLib = pluginManager.getPlugin("SkyLib");
        if (skyLib != null) {
            String version = skyLib.getPluginMeta().getVersion();
            String[] splitVersion = version.split("\\.");
            int second = Integer.parseInt(splitVersion[1]);

            if(second >= 3) {
                return true;
            }
        }

        this.getComponentLogger().error(AdventureUtil.serialize("SkyLib Version 1.3.0.0 or newer is required to run this plugin."));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }
}
