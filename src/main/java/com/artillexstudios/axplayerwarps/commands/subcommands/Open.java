package com.artillexstudios.axplayerwarps.commands.subcommands;

import com.artillexstudios.axplayerwarps.guis.framework.GuiManager;
import com.artillexstudios.axplayerwarps.warps.Warp;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static com.artillexstudios.axplayerwarps.AxPlayerWarps.CONFIG;
import static com.artillexstudios.axplayerwarps.AxPlayerWarps.MESSAGEUTILS;

public enum Open {
    INSTANCE;

    public void execute(CommandSender sender, @Nullable Warp warp) {
        if (warp != null) {
            if (!(sender instanceof Player pl)) {
                MESSAGEUTILS.sendLang(sender, "commands.player-only");
                return;
            }
            warp.teleportPlayer(pl);
            return;
        }
        execute(sender, (Player) null);
    }

    public void execute(CommandSender sender, @Nullable Player player) {
        Player openTo;
        if (player != null) {
            openTo = player;
        } else {
            if (sender instanceof Player pl) {
                openTo = pl;
            } else {
                MESSAGEUTILS.sendLang(sender, "commands.player-only");
                return;
            }
        }

        GuiManager.open(openTo, CONFIG.getString("default-gui", "categories"));
    }
}