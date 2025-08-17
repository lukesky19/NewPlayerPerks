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
import com.github.lukesky19.newPlayerPerks.listener.DamageListener;
import com.github.lukesky19.newPlayerPerks.listener.DeathListener;
import com.github.lukesky19.newPlayerPerks.listener.JoinListener;
import com.github.lukesky19.newPlayerPerks.listener.QuitListener;
import com.github.lukesky19.newPlayerPerks.manager.PerksManager;
import com.github.lukesky19.newPlayerPerks.manager.PlayerDataManager;
import com.github.lukesky19.newPlayerPerks.manager.TaskManager;
import com.github.lukesky19.newPlayerPerks.manager.config.LocaleManager;
import com.github.lukesky19.newPlayerPerks.manager.config.SettingsManager;
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
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * The main plugin class
 */
public final class NewPlayerPerks extends JavaPlugin {
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private PerksManager perksManager;
    private TaskManager taskManager;

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

        playerDataManager = new PlayerDataManager(this, databaseManager);
        perksManager = new PerksManager(this, settingsManager, localeManager, playerDataManager);
        taskManager = new TaskManager(this, settingsManager, playerDataManager, perksManager);

        taskManager.startCheckPerksTask();

        this.getServer().getPluginManager().registerEvents(new JoinListener(this, settingsManager, localeManager, playerDataManager, perksManager), this);
        this.getServer().getPluginManager().registerEvents(new QuitListener(playerDataManager, perksManager), this);
        this.getServer().getPluginManager().registerEvents(new DamageListener(perksManager), this);
        this.getServer().getPluginManager().registerEvents(new DeathListener(settingsManager, perksManager), this);

        NewPlayersPerksCommand newPlayersCommandCommand = new NewPlayersPerksCommand(this, settingsManager, localeManager, playerDataManager, perksManager);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands ->
                        commands.registrar().register(newPlayersCommandCommand.createCommand(),
                                "Command to manage and use the NewPlayerPerks plugin.",
                                List.of("npp", "perk", "perks")));

        // Create and register the NewPlayerPerksAPI
        NewPlayerPerksAPI newPlayerPerksAPI = new NewPlayerPerksAPI(settingsManager, playerDataManager, perksManager);
        this.getServer().getServicesManager().register(NewPlayerPerksAPI.class, newPlayerPerksAPI, this, ServicePriority.Lowest);

        reload();
    }

    /**
     * The method ran on plugin disable.
     */
    @Override
    public void onDisable() {
        if(taskManager != null) taskManager.stopCheckPerksTask();

        if(perksManager != null) perksManager.disableAllPerks(false);

        if(playerDataManager != null) {
            playerDataManager.savePlayerData().thenAccept(v -> {
                if(databaseManager != null) databaseManager.handlePluginDisable();
            });
        } else {
            if(databaseManager != null) databaseManager.handlePluginDisable();
        }
    }

    /**
     * Reloads all plugin data.
     */
    public void reload() {
        settingsManager.reload();
        localeManager.reload();
        perksManager.disableAllPerks(true);
        playerDataManager.reload().thenAccept(v -> perksManager.enableAllPerks());
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
