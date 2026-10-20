package com.artillexstudios.axplayerwarps.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Thin wrapper around a BoostedYAML document.
 */
public class Config {
    private YamlDocument configuration;

    public Config(File file) {
        this(file, null, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    public Config(File file, InputStream defaults, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) {
        try {
            configuration = YamlDocument.create(file, defaults, generalSettings, loaderSettings, dumperSettings, updaterSettings);
        } catch (Exception ex) {
            logInvalid(file, ex);
        }
    }

    private static void logInvalid(File file, Exception ex) {
        Bukkit.getLogger().log(Level.SEVERE, "[AxPlayerWarps] Failed to load " + file.getName() + "! Check the file for YAML errors (for example with https://yamlchecker.com).", ex);
    }

    public void set(String key, Object value) {
        configuration.set(key, value);
    }

    public boolean getBoolean(String key) {
        return configuration.getBoolean(key);
    }

    public boolean getBoolean(String key, boolean def) {
        return configuration.getBoolean(key, def);
    }

    public String getString(String key) {
        return configuration.getString(key);
    }

    public String getString(String key, String def) {
        return configuration.getString(key, def);
    }

    public int getInt(String key) {
        return configuration.getInt(key);
    }

    public int getInt(String key, int def) {
        return configuration.getInt(key, def);
    }

    public long getLong(String key) {
        return configuration.getLong(key);
    }

    public long getLong(String key, long def) {
        return configuration.getLong(key, def);
    }

    public double getDouble(String key) {
        return configuration.getDouble(key);
    }

    public double getDouble(String key, double def) {
        return configuration.getDouble(key, def);
    }

    public List<String> getStringList(String key) {
        return configuration.getStringList(key);
    }

    public <T, U> List<Map<T, U>> getMapList(String key) {
        List<Map<T, U>> listMap = new ArrayList<>();
        for (Map<?, ?> map : configuration.getMapList(key)) {
            HashMap<T, U> hashMap = new HashMap<>();
            map.forEach((k, v) -> hashMap.put((T) k, (U) v));
            listMap.add(hashMap);
        }
        return listMap;
    }

    public Section getSection(String key) {
        return configuration.getSection(key);
    }

    public boolean reload() {
        try {
            return configuration.reload();
        } catch (Exception ex) {
            logInvalid(configuration.getFile(), ex);
            return false;
        }
    }

    public void save() {
        try {
            configuration.save();
        } catch (Exception ex) {
            logInvalid(configuration.getFile(), ex);
        }
    }

    public YamlDocument getBackingDocument() {
        return configuration;
    }
}
