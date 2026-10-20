package com.artillexstudios.axplayerwarps.hooks.currency;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A currency warps can charge for teleporting. If the currency does not work offline,
 * giving to or taking from an offline player completes with false.
 */
public abstract class CurrencyHook {
    private final String name;
    private final String currency;

    protected CurrencyHook(@NotNull String name, @Nullable String currency) {
        this.name = name;
        this.currency = currency;
    }

    /**
     * The plugin's name, also used as the key in currencies.yml.
     */
    @NotNull
    public final String getName() {
        return name;
    }

    /**
     * The currency inside a multi-currency plugin, null otherwise.
     */
    @Nullable
    public final String getCurrency() {
        return currency;
    }

    /**
     * Name stored with warps: the plugin name, followed by the currency for multi-currency plugins.
     */
    @NotNull
    public final String getFormattedName() {
        if (currency == null) return name;
        return "%s-%s".formatted(name, currency);
    }

    public abstract boolean usesDecimals();

    /**
     * Only for display and quick checks, payments should rely on the futures of give and take.
     */
    public abstract double getBalance(@NotNull Player player);

    @NotNull
    public abstract CompletableFuture<Boolean> giveBalance(@NotNull UUID player, double amount);

    @NotNull
    public abstract CompletableFuture<Boolean> takeBalance(@NotNull UUID player, double amount);
}
