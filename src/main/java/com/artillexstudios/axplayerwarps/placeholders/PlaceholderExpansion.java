package com.artillexstudios.axplayerwarps.placeholders;

import com.artillexstudios.axplayerwarps.AxPlayerWarps;
import com.artillexstudios.axplayerwarps.warps.Warp;
import com.artillexstudios.axplayerwarps.warps.WarpManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Serves %axplayerwarps_<placeholder>% and %axplayerwarps_<warp>_<placeholder>% through PlaceholderAPI.
 */
public class PlaceholderExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "axplayerwarps";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", AxPlayerWarps.getInstance().getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return AxPlayerWarps.getInstance().getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        List<Object> parameters = new ArrayList<>(2);
        if (offlinePlayer != null && offlinePlayer.getPlayer() != null) parameters.add(offlinePlayer.getPlayer());

        Function<PlaceholderHandler.Context, String> parser = PlaceholderHandler.getExternal().get(params);
        if (parser != null) return parser.apply(new PlaceholderHandler.Context(parameters));

        // warp names can contain underscores, so match the placeholder from the end, longest first
        // (given_rating_stars before rating_stars)
        List<Map.Entry<String, Function<PlaceholderHandler.Context, String>>> entries = new ArrayList<>(PlaceholderHandler.getWarpArgument().entrySet());
        entries.sort(Comparator.comparingInt(entry -> -entry.getKey().length()));
        for (Map.Entry<String, Function<PlaceholderHandler.Context, String>> entry : entries) {
            String suffix = "_" + entry.getKey();
            if (!params.endsWith(suffix)) continue;
            Warp warp = WarpManager.getWarp(params.substring(0, params.length() - suffix.length()));
            if (warp == null) continue;
            parameters.add(warp);
            return entry.getValue().apply(new PlaceholderHandler.Context(parameters));
        }
        return null;
    }
}
