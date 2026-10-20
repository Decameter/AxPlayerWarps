package com.artillexstudios.axplayerwarps.commands.subcommands;

import com.artillexstudios.axplayerwarps.utils.StringUtils;
import org.bukkit.command.CommandSender;

import static com.artillexstudios.axplayerwarps.AxPlayerWarps.LANG;

public enum Help {
    INSTANCE;

    public void execute(CommandSender sender) {
        for (String m : LANG.getStringList("help")) {
            sender.sendMessage(StringUtils.format(m));
        }
    }
}