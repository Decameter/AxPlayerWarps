package com.artillexstudios.axplayerwarps.guis.framework.actions;

import com.artillexstudios.axplayerwarps.guis.framework.GuiFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * A click action written as "[id] arguments" in gui files.
 */
public interface Action {

    void run(Player player, GuiFrame<?> gui, InventoryClickEvent event, String arguments);
}
