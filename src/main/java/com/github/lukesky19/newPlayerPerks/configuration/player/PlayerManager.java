package com.github.lukesky19.newPlayerPerks.configuration.player;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.skylib.config.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;

public class PlayerManager {
    private final NewPlayerPerks newPlayerPerks;

    public PlayerManager(NewPlayerPerks newPlayerPerks) {
        this.newPlayerPerks = newPlayerPerks;
    }

    public void createPlayerSettings(Player player) {
        PlayerSettings playerSettings;

        if(!player.hasPlayedBefore()) {
            playerSettings = new PlayerSettings(System.currentTimeMillis());
        } else {
            playerSettings = new PlayerSettings(0);
        }

        Path path = Path.of(newPlayerPerks.getDataFolder() + File.separator + "playerdata" + File.separator + player.getUniqueId() + ".yml");

        if(!path.toFile().exists()) {
            savePlayerSettings(player, playerSettings);
        }
    }

    public void savePlayerSettings(Player player, PlayerSettings playerSettings) {
        Path path = Path.of(newPlayerPerks.getDataFolder() + File.separator + "playerdata" + File.separator + player.getUniqueId() + ".yml");
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);

        CommentedConfigurationNode playerNode = loader.createNode();
        try {
            playerNode.set(playerSettings);
            loader.save(playerNode);
        } catch(ConfigurateException e) {
            newPlayerPerks.getComponentLogger().error(MiniMessage.miniMessage().deserialize("<red>Unable to save " + player.getName() + "'s settings.</red>"));
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public PlayerSettings getPlayerSettings(Player player) {
        PlayerSettings playerSettings;
        Path path = Path.of(newPlayerPerks.getDataFolder() + File.separator + "playerdata" + File.separator + player.getUniqueId() + ".yml");

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            playerSettings = loader.load().get(PlayerSettings.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }

        return playerSettings;
    }
}
