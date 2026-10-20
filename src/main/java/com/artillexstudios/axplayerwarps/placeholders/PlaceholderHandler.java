package com.artillexstudios.axplayerwarps.placeholders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Plugin placeholders like %name%, resolved against the objects passed to {@link #parse(String, Object...)}
 * (a warp, the viewing player, ...). Placeholders registered as external are also served through PlaceholderAPI.
 */
public final class PlaceholderHandler {
    private static final Map<String, Function<Context, String>> internal = new LinkedHashMap<>();
    private static final Map<String, Function<Context, String>> external = new LinkedHashMap<>();
    // placeholders that are prefixed by a warp name in PlaceholderAPI: %axplayerwarps_<warp>_<key>%
    private static final Map<String, Function<Context, String>> warpArgument = new LinkedHashMap<>();

    private PlaceholderHandler() {
    }

    public static void register(String key, Function<Context, String> parser, boolean external) {
        internal.put(key, parser);
        if (external) PlaceholderHandler.external.put(key, parser);
    }

    public static void registerWarpArgument(String key, Function<Context, String> parser) {
        warpArgument.put(key, parser);
    }

    static Map<String, Function<Context, String>> getExternal() {
        return Collections.unmodifiableMap(external);
    }

    static Map<String, Function<Context, String>> getWarpArgument() {
        return Collections.unmodifiableMap(warpArgument);
    }

    @NotNull
    public static String parse(@NotNull String text, Object... parameters) {
        int start = text.indexOf('%');
        if (start == -1) return text;

        Context context = new Context(List.of(parameters));
        StringBuilder builder = new StringBuilder(text.length());
        int last = 0;
        while (start != -1) {
            int end = text.indexOf('%', start + 1);
            if (end == -1) break;

            Function<Context, String> parser = internal.get(text.substring(start + 1, end));
            if (parser == null) {
                // the closing % may open the next placeholder
                start = end;
                continue;
            }

            builder.append(text, last, start).append(parser.apply(context));
            last = end + 1;
            start = text.indexOf('%', last);
        }
        builder.append(text, last, text.length());
        return builder.toString();
    }

    public record Context(List<Object> parameters) {

        @Nullable
        public <T> T get(Class<T> clazz) {
            for (Object parameter : parameters) {
                if (clazz.isInstance(parameter)) return clazz.cast(parameter);
            }
            return null;
        }
    }
}
