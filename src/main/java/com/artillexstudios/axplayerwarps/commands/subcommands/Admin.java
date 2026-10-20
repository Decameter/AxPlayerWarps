package com.artillexstudios.axplayerwarps.commands.subcommands;

import com.artillexstudios.axplayerwarps.AxPlayerWarps;
import com.artillexstudios.axplayerwarps.enums.AccessList;
import com.artillexstudios.axplayerwarps.utils.StringUtils;
import com.artillexstudios.axplayerwarps.warps.Warp;
import com.artillexstudios.axplayerwarps.warps.WarpManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Map;

import static com.artillexstudios.axplayerwarps.AxPlayerWarps.LANG;
import static com.artillexstudios.axplayerwarps.AxPlayerWarps.MESSAGEUTILS;

public enum Admin {
    INSTANCE;

    public void help(CommandSender sender) {
        for (String m : LANG.getStringList("admin-help")) {
            sender.sendMessage(StringUtils.format(m));
        }
    }

    public void delete(CommandSender sender, Warp warp) {
        AxPlayerWarps.getThreadedQueue().submit(() -> {
            AxPlayerWarps.getDatabase().deleteWarp(warp);
            MESSAGEUTILS.sendLang(sender, "admin.deleted", Map.of("%warp%", warp.getName()));
        });
    }

    public void deleteId(CommandSender sender, int id) {
        AxPlayerWarps.getThreadedQueue().submit(() -> {
            Warp warp = null;
            for (Warp w : WarpManager.getWarps()) {
                if (id != w.getId()) continue;
                warp = w;
                break;
            }
            if (warp == null) {
                MESSAGEUTILS.sendLang(sender, "errors.not-found", Map.of("%warp%", String.valueOf(id)));
                return;
            }
            AxPlayerWarps.getDatabase().deleteWarp(warp);
            MESSAGEUTILS.sendLang(sender, "admin.deleted", Map.of("%warp%", warp.getName()));
        });
    }

    public void setOwner(CommandSender sender, Warp warp, OfflinePlayer player) {
        AxPlayerWarps.getThreadedQueue().submit(() -> {
            warp.setOwner(player.getUniqueId());
            AxPlayerWarps.getDatabase().updateWarp(warp);
            AxPlayerWarps.getDatabase().removeFromList(warp, AccessList.WHITELIST, player);
            AxPlayerWarps.getDatabase().removeFromList(warp, AccessList.BLACKLIST, player);

            MESSAGEUTILS.sendLang(sender, "admin.setowner", Map.of("%warp%", warp.getName(), "%player%", player.getName() == null ? "---" : player.getName()));
        });
    }
}
