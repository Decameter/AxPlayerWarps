package com.artillexstudios.axplayerwarps.hooks.currency;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerPointsHook extends CurrencyHook {
    private final PlayerPointsAPI api = PlayerPoints.getInstance().getAPI();

    public PlayerPointsHook() {
        super("PlayerPoints", null);
    }

    @Override
    public boolean usesDecimals() {
        return false;
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return api.look(player.getUniqueId());
    }

    @Override
    public @NotNull CompletableFuture<Boolean> giveBalance(@NotNull UUID uuid, double amount) {
        return CompletableFuture.completedFuture(api.give(uuid, (int) Math.round(amount)));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> takeBalance(@NotNull UUID uuid, double amount) {
        return CompletableFuture.completedFuture(api.take(uuid, (int) Math.round(amount)));
    }
}
