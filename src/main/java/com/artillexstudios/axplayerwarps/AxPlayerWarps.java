package com.artillexstudios.axplayerwarps;

import com.artillexstudios.axplayerwarps.category.CategoryManager;
import com.artillexstudios.axplayerwarps.commands.CommandManager;
import com.artillexstudios.axplayerwarps.config.Config;
import com.artillexstudios.axplayerwarps.database.Database;
import com.artillexstudios.axplayerwarps.database.impl.H2;
import com.artillexstudios.axplayerwarps.database.impl.MySQL;
import com.artillexstudios.axplayerwarps.database.impl.PostgreSQL;
import com.artillexstudios.axplayerwarps.guis.BlacklistGui;
import com.artillexstudios.axplayerwarps.guis.CategoryGui;
import com.artillexstudios.axplayerwarps.guis.EditWarpGui;
import com.artillexstudios.axplayerwarps.guis.FavoritesGui;
import com.artillexstudios.axplayerwarps.guis.MyWarpsGui;
import com.artillexstudios.axplayerwarps.guis.RateWarpGui;
import com.artillexstudios.axplayerwarps.guis.RecentsGui;
import com.artillexstudios.axplayerwarps.guis.WarpsGui;
import com.artillexstudios.axplayerwarps.guis.WhitelistGui;
import com.artillexstudios.axplayerwarps.guis.framework.GuiManager;
import com.artillexstudios.axplayerwarps.hooks.HookManager;
import com.artillexstudios.axplayerwarps.input.InputListener;
import com.artillexstudios.axplayerwarps.listeners.MoveListener;
import com.artillexstudios.axplayerwarps.listeners.PlayerListeners;
import com.artillexstudios.axplayerwarps.listeners.WorldListeners;
import com.artillexstudios.axplayerwarps.placeholders.PlaceholderExpansion;
import com.artillexstudios.axplayerwarps.placeholders.WarpPlaceholders;
import com.artillexstudios.axplayerwarps.sorting.SortingManager;
import com.artillexstudios.axplayerwarps.utils.AsyncUtils;
import com.artillexstudios.axplayerwarps.utils.DataQueue;
import com.artillexstudios.axplayerwarps.utils.MessageUtils;
import com.artillexstudios.axplayerwarps.utils.Scheduler;
import com.artillexstudios.axplayerwarps.utils.StringUtils;
import com.artillexstudios.axplayerwarps.utils.UpdateNotifier;
import com.artillexstudios.axplayerwarps.warps.WarpManager;
import com.artillexstudios.axplayerwarps.warps.WarpQueue;
import com.artillexstudios.axplayerwarps.world.WorldManager;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class AxPlayerWarps extends JavaPlugin {
    private static AxPlayerWarps instance;
    private static DataQueue threadedQueue;
    private static Database database;
    public static MessageUtils MESSAGEUTILS;
    public static Config CONFIG;
    public static Config HOOKS;
    public static Config LANG;
    public static Config CURRENCIES;
    public static Config INPUT;

    public static DataQueue getThreadedQueue() {
        return threadedQueue;
    }

    public static Database getDatabase() {
        return database;
    }

    public static AxPlayerWarps getInstance() {
        return instance;
    }

    // todo future plans
    // - desc color codes
    // - teleport price tax
    @Override
    public void onEnable() {
        instance = this;
        new Metrics(this, 21645);

        CONFIG = new Config(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
        HOOKS = new Config(new File(getDataFolder(), "hooks.yml"), getResource("hooks.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
        LANG = new Config(new File(getDataFolder(), "lang.yml"), getResource("lang.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        CURRENCIES = new Config(new File(getDataFolder(), "currencies.yml"), getResource("currencies.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        INPUT = new Config(new File(getDataFolder(), "input.yml"), getResource("input.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());

        threadedQueue = new DataQueue("AxPlayerWarps-Datastore-thread");
        AsyncUtils.start(CONFIG.getInt("gui-loading-threads", 2));

        MESSAGEUTILS = new MessageUtils(LANG, "prefix", CONFIG);

        CategoryGui.reload();
        GuiManager.registerGuiType("categories", CategoryGui::new);
        WarpsGui.reload();
        GuiManager.registerGuiType("warps", WarpsGui::new);
        RateWarpGui.reload();
        EditWarpGui.reload();
        FavoritesGui.reload();
        GuiManager.registerGuiType("favorites", FavoritesGui::new);
        RecentsGui.reload();
        GuiManager.registerGuiType("recents", RecentsGui::new);
        MyWarpsGui.reload();
        GuiManager.registerGuiType("my-warps", MyWarpsGui::new);
        WhitelistGui.reload();
        BlacklistGui.reload();

        WarpPlaceholders.load();
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderExpansion().register();
        }

        switch (CONFIG.getString("database.type").toLowerCase()) {
            case "mysql" -> database = new MySQL();
            case "postgresql" -> database = new PostgreSQL();
            default -> database = new H2();
        }

        database.setup();

        // runs once the server has finished starting, after the hooked plugins are enabled
        Scheduler.run(HookManager::setupHooks);

        WorldManager.reload();
        CategoryManager.reload();
        SortingManager.reload();

        CommandManager.load();

        WarpManager.load();
        WarpQueue.start();

        getServer().getPluginManager().registerEvents(new WorldListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new MoveListener(), this);
        getServer().getPluginManager().registerEvents(new InputListener(), this);

        Bukkit.getConsoleSender().sendMessage(StringUtils.format("&#44f1d7[AxPlayerWarps] Loaded plugin! Using &f" + database.getType() + " &#44f1d7database to store data!"));

        UpdateNotifier.init(CONFIG, LANG);
        if (CONFIG.getBoolean("update-notifier.enabled", true)) new UpdateNotifier();
    }

    @Override
    public void onDisable() {
        AsyncUtils.stop();
        if (threadedQueue != null) threadedQueue.stop();
        if (database != null) database.disable();
    }
}
