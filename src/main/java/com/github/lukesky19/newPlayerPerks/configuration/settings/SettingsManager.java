package com.github.lukesky19.newPlayerPerks.configuration.settings;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.skylib.config.ConfigurationUtility;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

public class SettingsManager {
    private final NewPlayerPerks newPlayerPerks;
    private Settings settings;
    private long period;

    public SettingsManager(NewPlayerPerks newPlayerPerks) {
        this.newPlayerPerks = newPlayerPerks;
    }

    public Settings getSettings() {
        return settings;
    }

    public long getPeriod() {
        return period;
    }

    public void reload() {
        Path path = Path.of(newPlayerPerks.getDataFolder() + File.separator + "settings.yml");

        if(!path.toFile().exists()) {
            newPlayerPerks.saveResource("settings.yml", false);
        }

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            settings = loader.load().get(Settings.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }

        if (settings != null) {
            period = FormatUtil.stringToMillis(settings.period());
        }
    }
}
