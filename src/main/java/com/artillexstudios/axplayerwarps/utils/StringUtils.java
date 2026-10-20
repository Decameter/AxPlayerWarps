package com.artillexstudios.axplayerwarps.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Formats config strings that mix legacy codes (&a, &l, &#RRGGBB) with MiniMessage tags.
 */
public final class StringUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern UNUSUAL_LEGACY_HEX_PATTERN = Pattern.compile("&x&([a-fA-F0-9])&([a-fA-F0-9])&([a-fA-F0-9])&([a-fA-F0-9])&([a-fA-F0-9])&([a-fA-F0-9])");
    private static final String COLOR_CHARS = "0123456789abcdef#";
    private static final String[][] COLOR_FORMATS = {
            {"&0", "<black>"}, {"&1", "<dark_blue>"}, {"&2", "<dark_green>"}, {"&3", "<dark_aqua>"},
            {"&4", "<dark_red>"}, {"&5", "<dark_purple>"}, {"&6", "<gold>"}, {"&7", "<gray>"},
            {"&8", "<dark_gray>"}, {"&9", "<blue>"}, {"&a", "<green>"}, {"&b", "<aqua>"},
            {"&c", "<red>"}, {"&d", "<light_purple>"}, {"&e", "<yellow>"}, {"&f", "<white>"},
            {"&r", "<reset>"}, {"\n", "<br>"}
    };
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .useUnusualXRepeatedCharacterHexFormat()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .build();

    private StringUtils() {
    }

    @NotNull
    public static Component format(@NotNull String input) {
        return MINI_MESSAGE.deserialize(toMiniMessage(input)).applyFallbackStyle(TextDecoration.ITALIC.withState(false));
    }

    @NotNull
    public static Component format(@NotNull String input, @NotNull Map<String, String> replacements) {
        return format(replace(input, replacements));
    }

    @NotNull
    public static String formatToString(@NotNull String input) {
        return LEGACY_SERIALIZER.serialize(format(input));
    }

    @NotNull
    public static String formatToString(@NotNull String input, @NotNull Map<String, String> replacements) {
        return formatToString(replace(input, replacements));
    }

    @NotNull
    public static List<Component> formatList(@NotNull List<String> list) {
        List<Component> newList = new ArrayList<>(list.size());
        for (String line : list) {
            newList.add(format(line));
        }
        return newList;
    }

    @NotNull
    public static String replace(@NotNull String input, @NotNull Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }

    private static String toMiniMessage(String input) {
        String toFormat = input.replace(LegacyComponentSerializer.SECTION_CHAR, '&');

        toFormat = replaceLegacyFormat(toFormat, "&l", "<b>", "</b>");
        toFormat = replaceLegacyFormat(toFormat, "&m", "<st>", "</st>");
        toFormat = replaceLegacyFormat(toFormat, "&n", "<u>", "</u>");
        toFormat = replaceLegacyFormat(toFormat, "&o", "<i>", "</i>");
        toFormat = replaceLegacyFormat(toFormat, "&k", "<obf>", "</obf>");

        toFormat = HEX_PATTERN.matcher(toFormat).replaceAll(match -> "<#" + match.group(1) + ">");
        toFormat = UNUSUAL_LEGACY_HEX_PATTERN.matcher(toFormat).replaceAll(match -> "<#" + match.group(1) + match.group(2) + match.group(3) + match.group(4) + match.group(5) + match.group(6) + ">");

        for (String[] format : COLOR_FORMATS) {
            toFormat = toFormat.replace(format[0], format[1]);
        }

        return toFormat;
    }

    // closes a legacy decoration (like &l) at the next color code, the same way legacy formatting resets it
    private static String replaceLegacyFormat(String toFormat, String search, String start, String close) {
        int index;
        while ((index = toFormat.indexOf(search)) != -1) {
            StringBuilder builder = new StringBuilder(toFormat).replace(index, index + search.length(), start);
            for (int i = index + start.length(); i < builder.length() - 1; i++) {
                if (builder.charAt(i) == '&' && COLOR_CHARS.indexOf(builder.charAt(i + 1)) != -1) {
                    builder.insert(i, close);
                    break;
                }
            }
            toFormat = builder.toString();
        }
        return toFormat;
    }
}
