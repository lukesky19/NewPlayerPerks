package com.github.lukesky19.newPlayerPerks.configuration.locale;

import com.github.lukesky19.newPlayerPerks.NewPlayerPerks;
import com.github.lukesky19.newPlayerPerks.configuration.settings.SettingsManager;
import com.github.lukesky19.skylib.config.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

public class LocaleManager {
    private final NewPlayerPerks newPlayerPerks;
    private final SettingsManager settingsManager;
    private Locale locale;

    public LocaleManager(NewPlayerPerks newPlayerPerks, SettingsManager settingsManager) {
        this.newPlayerPerks = newPlayerPerks;
        this.settingsManager = settingsManager;
    }

    public Locale getLocale() {
        return locale;
    }

    public void reload() {
        String localeString = settingsManager.getSettings().locale();
        Path path = Path.of(newPlayerPerks.getDataFolder() + File.separator + "locale" + File.separator + localeString + ".yml");

        copyDefaultLocales();

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            locale = loader.load().get(Locale.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyDefaultLocales() {
        Path path = Path.of(newPlayerPerks.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            newPlayerPerks.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }
}
