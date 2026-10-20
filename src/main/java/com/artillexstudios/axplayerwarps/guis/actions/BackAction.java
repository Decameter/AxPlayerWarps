package com.artillexstudios.axplayerwarps.guis.actions;

import com.artillexstudios.axplayerwarps.guis.framework.GuiFrame;
import com.artillexstudios.axplayerwarps.guis.framework.actions.Action;
import com.artillexstudios.axplayerwarps.user.Users;
import com.artillexstudios.axplayerwarps.user.WarpUser;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Deque;

public class BackAction implements Action {

    @Override
    public void run(Player player, GuiFrame<?> gui, InventoryClickEvent event, String arguments) {
        WarpUser user = Users.get(player);
        Deque<GuiFrame<?>> last = user.getLastGuis();

        // the current gui is the last one
        if (last.pollLast() == null) return;

        GuiFrame<?> previous = last.peekLast();
        if (previous == null) {
            player.closeInventory();
            return;
        }

        previous.open();
    }
}
