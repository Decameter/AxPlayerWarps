package com.artillexstudios.axplayerwarps.guis.framework;

import com.artillexstudios.axplayerwarps.config.Config;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.entity.Player;

public abstract class PaginatedGuiFrame extends GuiFrame<PaginatedGui> {

    public PaginatedGuiFrame(int updateTicks, Config config, Player player) {
        super(updateTicks, config, player);
    }

    public void open(int page) {
        gui.open(player, page);
    }
}
