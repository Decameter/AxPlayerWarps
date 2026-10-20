package com.artillexstudios.axplayerwarps.guis.actions;

import com.artillexstudios.axplayerwarps.category.Category;
import com.artillexstudios.axplayerwarps.category.CategoryManager;
import com.artillexstudios.axplayerwarps.guis.WarpsGui;
import com.artillexstudios.axplayerwarps.guis.framework.GuiFrame;
import com.artillexstudios.axplayerwarps.guis.framework.actions.Action;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CategoryAction implements Action {

    @Override
    public void run(Player player, GuiFrame<?> gui, InventoryClickEvent event, String arguments) {
        Category category = CategoryManager.getCategories().get(arguments);
        if (category == null) {
            new WarpsGui(player).open();
        } else {
            new WarpsGui(player, category, null).open();
        }
    }
}
