package com.artillexstudios.axplayerwarps.guis;

import com.artillexstudios.axplayerwarps.AxPlayerWarps;
import com.artillexstudios.axplayerwarps.config.Config;
import com.artillexstudios.axplayerwarps.guis.framework.GuiFrame;
import com.artillexstudios.axplayerwarps.user.Users;
import com.artillexstudios.axplayerwarps.user.WarpUser;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.io.File;

public class CategoryGui extends GuiFrame<Gui> {
    private static final Config GUI = new Config(new File(AxPlayerWarps.getInstance().getDataFolder(), "guis/categories.yml"),
            AxPlayerWarps.getInstance().getResource("guis/categories.yml"),
            GeneralSettings.builder().setUseDefaults(false).build(),
            LoaderSettings.builder().build(),
            DumperSettings.DEFAULT,
            UpdaterSettings.builder().build()
    );

    private final WarpUser user;

    public CategoryGui(Player player) {
        super(GUI.getInt("auto-update-ticks", -1), GUI, player);
        this.user = Users.get(player);

        gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.empty())
                .rows(GUI.getInt("rows", 5))
                .create();

        setGui(gui, () -> parseText(GUI.getString("title", "")));
        user.addGui(this);
    }

    public static boolean reload() {
        return GUI.reload();
    }

    public void open() {
        gui.update();
        gui.open(player);
    }
}
