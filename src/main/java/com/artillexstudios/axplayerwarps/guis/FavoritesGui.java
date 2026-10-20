package com.artillexstudios.axplayerwarps.guis;

import com.artillexstudios.axplayerwarps.AxPlayerWarps;
import com.artillexstudios.axplayerwarps.config.Config;
import com.artillexstudios.axplayerwarps.guis.framework.AxGuiItem;
import com.artillexstudios.axplayerwarps.guis.framework.PaginatedGuiFrame;
import com.artillexstudios.axplayerwarps.guis.framework.Replacements;
import com.artillexstudios.axplayerwarps.user.Users;
import com.artillexstudios.axplayerwarps.user.WarpUser;
import com.artillexstudios.axplayerwarps.utils.ItemBuilder;
import com.artillexstudios.axplayerwarps.utils.Scheduler;
import com.artillexstudios.axplayerwarps.warps.Warp;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axplayerwarps.AxPlayerWarps.CONFIG;

public class FavoritesGui extends PaginatedGuiFrame {
    private static final Config GUI = new Config(new File(AxPlayerWarps.getInstance().getDataFolder(), "guis/favorites.yml"),
            AxPlayerWarps.getInstance().getResource("guis/favorites.yml"),
            GeneralSettings.builder().setUseDefaults(false).build(),
            LoaderSettings.builder().build(),
            DumperSettings.DEFAULT,
            UpdaterSettings.builder().build()
    );

    private final PaginatedGui gui;
    private final WarpUser user;

    public FavoritesGui(Player player) {
        super(GUI.getInt("auto-update-ticks", -1), GUI, player);
        this.user = Users.get(player);

        gui = Gui.paginated()
                .disableAllInteractions()
                .title(Component.empty())
                .rows(GUI.getInt("rows", 5))
                .pageSize(GUI.getInt("page-size", 27))
                .create();

        addReplacement(new Replacements("%page%", () -> String.valueOf(gui.getCurrentPageNum())));
        addReplacement(new Replacements("%current_page%", () -> String.valueOf(gui.getCurrentPageNum())));
        addReplacement(new Replacements("%max_page%", () -> String.valueOf(gui.getPagesNum())));
        addReplacement(new Replacements("%pages%", () -> String.valueOf(gui.getPagesNum())));

        setGui(gui, () -> parseText(GUI.getString("title", "")));
        user.addGui(this);
    }

    public static boolean reload() {
        return GUI.reload();
    }

    public void open() {
        load().thenRun(() -> {
            updateTitle();
            gui.open(player);
        });
    }

    public void update() {
        load().thenRun(() -> {
            gui.update();
        });
    }

    public CompletableFuture<Void> load() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        AxPlayerWarps.getThreadedQueue().submit(() -> {
            gui.clearPageItems();
            for (Warp warp : user.getFavorites()) {
                Material icon = warp.getIcon();
                ItemBuilder builder = ItemBuilder.create(new ItemStack(icon));
                builder.setName(parseText(GUI.getString("warp.name"), warp));

                String[] description = warp.getDescription().split("\n", CONFIG.getInt("warp-description.max-lines", 3));

                List<String> lore = new ArrayList<>();
                List<String> lore2 = new ArrayList<>(GUI.getStringList("warp.lore"));
                for (int i = 0; i < lore2.size(); i++) {
                    String line = lore2.get(i);
                    if (!line.contains("%description%")) {
                        lore.add(line);
                        continue;
                    }
                    for (int j = description.length - 1; j >= 0; j--) {
                        lore.add(i, line.replace("%description%", description[j]));
                    }
                }
                builder.setLore(parseText(lore, warp));
                if (icon == Material.PLAYER_HEAD) {
                    Player pl = Bukkit.getPlayer(warp.getOwner());
                    if (pl != null) {
                        String texture = ItemBuilder.getTexture(pl);
                        if (texture != null) builder.setTextureValue(texture);
                    }
                }
                gui.addItem(new AxGuiItem(builder.get(), event -> {
                    if (event.isLeftClick()) {
                        warp.teleportPlayer(player);
                    } else {
                        new RateWarpGui(player, warp).open();
                    }
                }));
            }

            Scheduler.run(player, () -> {
                future.complete(null);
            });
        });

        return future;
    }
}
