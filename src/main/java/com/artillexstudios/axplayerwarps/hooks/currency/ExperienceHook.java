package com.artillexstudios.axplayerwarps.hooks.currency;

import com.artillexstudios.axplayerwarps.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Experience points ("Experience") or whole levels ("Level") of online players.
 */
public class ExperienceHook extends CurrencyHook {
    private final boolean levels;

    public ExperienceHook(boolean levels) {
        super(levels ? "Level" : "Experience", null);
        this.levels = levels;
    }

    @Override
    public boolean usesDecimals() {
        return false;
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return levels ? player.getLevel() : player.calculateTotalExperiencePoints();
    }

    @Override
    public @NotNull CompletableFuture<Boolean> giveBalance(@NotNull UUID uuid, double amount) {
        return change(uuid, (int) Math.round(amount));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> takeBalance(@NotNull UUID uuid, double amount) {
        return change(uuid, -(int) Math.round(amount));
    }

    private CompletableFuture<Boolean> change(UUID uuid, int amount) {
        Player player = Bukkit.getPlayer(uuid);
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if (player == null) {
            future.complete(false);
            return future;
        }

        Scheduler.run(player, () -> {
            int balance = (int) getBalance(player);
            if (balance + amount < 0) {
                future.complete(false);
                return;
            }
            if (levels) {
                player.setLevel(balance + amount);
            } else {
                player.setExperienceLevelAndProgress(balance + amount);
            }
            future.complete(true);
        });
        return future;
    }
}
