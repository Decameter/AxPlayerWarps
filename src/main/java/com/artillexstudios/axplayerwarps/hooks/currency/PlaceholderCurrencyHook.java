package com.artillexstudios.axplayerwarps.hooks.currency;

import com.artillexstudios.axplayerwarps.utils.Scheduler;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A currency read through a PlaceholderAPI placeholder and changed with console commands.
 */
public class PlaceholderCurrencyHook extends CurrencyHook {
    private static final DecimalFormat df = new DecimalFormat("#");
    private final Section section;

    public PlaceholderCurrencyHook(String name, Section section) {
        super(name, null);
        this.section = section;
    }

    @Override
    public boolean usesDecimals() {
        return section.getBoolean("uses-double", false);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        String placeholder = section.getString("settings.raw-placeholder");
        try {
            return Double.parseDouble(PlaceholderAPI.setPlaceholders(player, placeholder));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public @NotNull CompletableFuture<Boolean> giveBalance(@NotNull UUID uuid, double amount) {
        return runCommand(uuid, amount, "settings.give-command");
    }

    @Override
    public @NotNull CompletableFuture<Boolean> takeBalance(@NotNull UUID uuid, double amount) {
        return runCommand(uuid, amount, "settings.take-command");
    }

    private CompletableFuture<Boolean> runCommand(UUID uuid, double amount, String route) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.getName() == null) return CompletableFuture.completedFuture(false);
        if (!player.isOnline() && !section.getBoolean("works-offline", false)) return CompletableFuture.completedFuture(false);

        String number = usesDecimals() ? String.valueOf(amount) : df.format(Math.round(amount));
        String command = section.getString(route)
                .replace("%amount%", number)
                .replace("%price%", number)
                .replace("%player%", player.getName());
        Scheduler.run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        return CompletableFuture.completedFuture(true);
    }
}
