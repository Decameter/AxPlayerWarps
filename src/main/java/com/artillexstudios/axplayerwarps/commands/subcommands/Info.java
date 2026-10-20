package com.artillexstudios.axplayerwarps.commands.subcommands;

import com.artillexstudios.axplayerwarps.placeholders.PlaceholderHandler;
import com.artillexstudios.axplayerwarps.utils.StringUtils;
import com.artillexstudios.axplayerwarps.warps.Warp;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.artillexstudios.axplayerwarps.AxPlayerWarps.CONFIG;
import static com.artillexstudios.axplayerwarps.AxPlayerWarps.LANG;

public enum Info {
    INSTANCE;

    public void execute(CommandSender sender, Warp warp) {
        String[] description = warp.getDescription().split("\n", CONFIG.getInt("warp-description.max-lines", 3));
        List<String> lore = new ArrayList<>();
        List<String> lore2 = new ArrayList<>(LANG.getStringList("info"));
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

        for (String s : lore) {
            sender.sendMessage(StringUtils.format(PlaceholderHandler.parse(s, warp)));
        }
    }
}