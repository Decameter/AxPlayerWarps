package com.artillexstudios.axplayerwarps.utils;

import com.artillexstudios.axplayerwarps.config.Config;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class MessageUtils {
    private final Config lang;
    private final Config prefixConfig;
    private final String prefixRoute;

    public MessageUtils(Config lang, String prefixRoute, Config prefixConfig) {
        this.lang = lang;
        this.prefixRoute = prefixRoute;
        this.prefixConfig = prefixConfig;
    }

    public void sendFormatted(CommandSender sender, String message) {
        if (sender == null || message == null || message.isEmpty()) return;
        sender.sendMessage(StringUtils.format(message));
    }

    public void sendFormatted(CommandSender sender, String message, Map<String, String> replacements) {
        if (message == null || message.isEmpty()) return;
        sendFormatted(sender, StringUtils.replace(message, replacements));
    }

    public void sendLang(CommandSender sender, String route) {
        sendLang(sender, route, Map.of());
    }

    public void sendLang(CommandSender sender, String route, Map<String, String> replacements) {
        String message = lang.getString(route);
        if (message == null || message.isEmpty()) return;
        sendFormatted(sender, prefixConfig.getString(prefixRoute, "") + message, replacements);
    }
}
