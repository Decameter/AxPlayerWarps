package com.artillexstudios.axplayerwarps.hooks.currency;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VaultHook extends CurrencyHook {
    private final Economy economy;

    private VaultHook(Economy economy) {
        super("Vault", null);
        this.economy = economy;
    }

    /**
     * @return null if no economy plugin registered itself with Vault
     */
    public static VaultHook create() {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider == null) return null;
        return new VaultHook(provider.getProvider());
    }

    @Override
    public boolean usesDecimals() {
        return true;
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return economy.getBalance(player);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> giveBalance(@NotNull UUID uuid, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return CompletableFuture.completedFuture(economy.depositPlayer(player, amount).transactionSuccess());
    }

    @Override
    public @NotNull CompletableFuture<Boolean> takeBalance(@NotNull UUID uuid, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return CompletableFuture.completedFuture(economy.withdrawPlayer(player, amount).transactionSuccess());
    }
}
