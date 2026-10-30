package com.artillexstudios.axplayerwarps.hooks;

import com.artillexstudios.axplayerwarps.hooks.currency.CurrencyHook;
import com.artillexstudios.axplayerwarps.hooks.currency.ExperienceHook;
import com.artillexstudios.axplayerwarps.hooks.currency.PlaceholderCurrencyHook;
import com.artillexstudios.axplayerwarps.hooks.currency.PlayerPointsHook;
import com.artillexstudios.axplayerwarps.hooks.currency.TokenManagerHook;
import com.artillexstudios.axplayerwarps.hooks.currency.VaultHook;
import com.artillexstudios.axplayerwarps.hooks.protection.BentoBoxHook;
import com.artillexstudios.axplayerwarps.hooks.protection.GriefPreventionHook;
import com.artillexstudios.axplayerwarps.hooks.protection.HuskClaimsHook;
import com.artillexstudios.axplayerwarps.hooks.protection.HuskTownsHook;
import com.artillexstudios.axplayerwarps.hooks.protection.IslandsHook;
import com.artillexstudios.axplayerwarps.hooks.protection.LandsHook;
import com.artillexstudios.axplayerwarps.hooks.protection.PlotSquaredHook;
import com.artillexstudios.axplayerwarps.hooks.protection.ProtectionHook;
import com.artillexstudios.axplayerwarps.hooks.protection.SuperiorSkyblockHook;
import com.artillexstudios.axplayerwarps.hooks.protection.TownyHook;
import com.artillexstudios.axplayerwarps.hooks.protection.WorldGuardHook;
import com.artillexstudios.axplayerwarps.utils.StringUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.artillexstudios.axplayerwarps.AxPlayerWarps.CURRENCIES;
import static com.artillexstudios.axplayerwarps.AxPlayerWarps.HOOKS;

/**
 * Currency and protection hooks. Each hook is only created when its plugin is enabled, and can be turned off in
 * currencies.yml ("register: false") or hooks.yml ("hooks.protection.<plugin>: false").
 */
public class HookManager {
    private static final Map<String, CurrencyOptions> currencyOptions = new HashMap<>();
    private static final List<CurrencyHook> currencies = new ArrayList<>();
    private static final Map<String, ProtectionHook> protections = new LinkedHashMap<>();

    public record CurrencyOptions(String displayName) {}

    /**
     * Should run after every plugin is enabled, so economies registered with Vault are found.
     */
    public static void setupHooks() {
        Map<String, Supplier<CurrencyHook>> available = availableCurrencies();
        boolean modified = false;
        for (String name : available.keySet()) {
            String route = "currencies." + name;
            if (CURRENCIES.getSection(route) != null) continue;
            CURRENCIES.set(route + ".register", true);
            CURRENCIES.set(route + ".name", "%price% money");
            modified = true;
        }
        if (modified) CURRENCIES.save();

        modified = false;
        for (String name : availableProtections().keySet()) {
            String route = "hooks.protection." + name;
            if (HOOKS.getString(route) != null) continue;
            HOOKS.set(route, true);
            modified = true;
        }
        if (modified) HOOKS.save();

        updateHooks();
    }

    public static void updateHooks() {
        currencies.clear();
        currencyOptions.clear();
        protections.clear();

        for (Map.Entry<String, Supplier<CurrencyHook>> entry : availableCurrencies().entrySet()) {
            String route = "currencies." + entry.getKey();
            if (!CURRENCIES.getBoolean(route + ".register", true)) continue;
            CurrencyHook hook = create(entry.getKey(), entry.getValue());
            if (hook == null) continue;
            currencies.add(hook);
            currencyOptions.put(hook.getFormattedName(), new CurrencyOptions(CURRENCIES.getString(route + ".name", "%price%")));
        }

        Section placeholderCurrencies = CURRENCIES.getSection("placeholder-currencies");
        if (placeholderCurrencies != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            for (String route : placeholderCurrencies.getRoutesAsStrings(false)) {
                Section section = placeholderCurrencies.getSection(route);
                if (section == null || !section.getBoolean("register", false)) continue;
                currencies.add(new PlaceholderCurrencyHook(route, section));
                currencyOptions.put(route, new CurrencyOptions(section.getString("name", "%price% money")));
            }
        }

        for (Map.Entry<String, Supplier<ProtectionHook>> entry : availableProtections().entrySet()) {
            if (!HOOKS.getBoolean("hooks.protection." + entry.getKey(), true)) continue;
            ProtectionHook hook = create(entry.getKey(), entry.getValue());
            if (hook != null) protections.put(entry.getKey(), hook);
        }

        List<String> names = new ArrayList<>();
        for (CurrencyHook hook : currencies) names.add(hook.getFormattedName());
        names.addAll(protections.keySet());
        if (!names.isEmpty()) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.format("&#33EEBB[AxPlayerWarps] Hooked into &f" + String.join(", ", names) + "&#33EEBB!"));
        }
    }

    // the suppliers keep the hook classes from loading unless their plugin is installed
    private static Map<String, Supplier<CurrencyHook>> availableCurrencies() {
        Map<String, Supplier<CurrencyHook>> map = new LinkedHashMap<>();
        map.put("Experience", () -> new ExperienceHook(false));
        map.put("Level", () -> new ExperienceHook(true));
        if (enabled("Vault")) map.put("Vault", VaultHook::create);
        if (enabled("PlayerPoints")) map.put("PlayerPoints", PlayerPointsHook::new);
        if (enabled("TokenManager")) map.put("TokenManager", TokenManagerHook::new);
        return map;
    }

    private static Map<String, Supplier<ProtectionHook>> availableProtections() {
        Map<String, Supplier<ProtectionHook>> map = new LinkedHashMap<>();
        if (enabled("WorldGuard")) map.put("WorldGuard", WorldGuardHook::new);
        if (enabled("GriefPrevention")) map.put("GriefPrevention", GriefPreventionHook::new);
        if (enabled("Lands")) map.put("Lands", LandsHook::new);
        if (enabled("Towny")) map.put("Towny", TownyHook::new);
        if (enabled("PlotSquared")) map.put("PlotSquared", PlotSquaredHook::new);
        if (enabled("HuskClaims")) map.put("HuskClaims", HuskClaimsHook::new);
        if (enabled("HuskTowns")) map.put("HuskTowns", HuskTownsHook::new);
        if (enabled("SuperiorSkyblock2")) map.put("SuperiorSkyblock2", SuperiorSkyblockHook::new);
        if (enabled("BentoBox")) map.put("BentoBox", BentoBoxHook::new);
        if (enabled("Islands")) map.put("Islands", IslandsHook::new);
        return map;
    }

    private static boolean enabled(String plugin) {
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }

    @Nullable
    private static <T> T create(String name, Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            // usually an incompatible version of the plugin
            Bukkit.getConsoleSender().sendMessage(StringUtils.format("&#FF0000[AxPlayerWarps] Failed to hook into " + name + ": " + throwable));
            return null;
        }
    }

    public static List<CurrencyHook> getCurrencies() {
        return currencies;
    }

    /**
     * Finds a currency by its plugin name or by its formatted name.
     */
    @Nullable
    public static CurrencyHook getCurrency(@Nullable String name) {
        if (name == null) return null;
        for (CurrencyHook hook : currencies) {
            if (hook.getFormattedName().equalsIgnoreCase(name)) return hook;
        }
        for (CurrencyHook hook : currencies) {
            if (hook.getName().equalsIgnoreCase(name)) return hook;
        }
        return null;
    }

    public static CurrencyOptions getCurrencyOptions(CurrencyHook hook) {
        return currencyOptions.get(hook.getFormattedName());
    }

    public static boolean canBreak(@NotNull Player player, @NotNull Location location) {
        for (ProtectionHook hook : protections.values()) {
            if (!hook.canBreak(player, location)) return false;
        }
        return true;
    }
}
