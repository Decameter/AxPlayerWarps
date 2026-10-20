package com.artillexstudios.axplayerwarps.guis.framework;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A gui item whose click action is ignored while the player is on the click cooldown.
 */
public class AxGuiItem extends GuiItem {

    public AxGuiItem(@NotNull ItemStack itemStack, @Nullable GuiAction<InventoryClickEvent> action) {
        super(itemStack, event -> {
            if (GuiManager.isOnCooldown(event.getWhoClicked())) {
                event.setCancelled(true);
                return;
            }
            if (action != null) action.execute(event);
        });
    }
}
