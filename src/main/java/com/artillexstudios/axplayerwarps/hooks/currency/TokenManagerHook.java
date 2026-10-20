package com.artillexstudios.axplayerwarps.hooks.currency;

import me.realized.tokenmanager.api.TokenManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TokenManagerHook extends CurrencyHook {
    private final TokenManager api = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");

    public TokenManagerHook() {
        super("TokenManager", null);
    }

    @Override
    public boolean usesDecimals() {
        return false;
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return api.getTokens(player).orElse(0);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> giveBalance(@NotNull UUID uuid, double amount) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return CompletableFuture.completedFuture(false);
        return CompletableFuture.completedFuture(api.addTokens(player, Math.round(amount)));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> takeBalance(@NotNull UUID uuid, double amount) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return CompletableFuture.completedFuture(false);
        return CompletableFuture.completedFuture(api.removeTokens(player, Math.round(amount)));
    }
}
