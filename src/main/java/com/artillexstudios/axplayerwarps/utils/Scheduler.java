package com.artillexstudios.axplayerwarps.utils;

import com.artillexstudios.axplayerwarps.AxPlayerWarps;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Paper's region-aware schedulers, which also work on Folia. Delays are in ticks.
 */
public final class Scheduler {

    private Scheduler() {
    }

    public static void run(Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().run(AxPlayerWarps.getInstance(), task -> runnable.run());
    }

    public static ScheduledTask runTimer(Runnable runnable, long delay, long period) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(AxPlayerWarps.getInstance(), task -> runnable.run(), Math.max(1, delay), period);
    }

    public static void runAt(Location location, Runnable runnable) {
        Bukkit.getRegionScheduler().run(AxPlayerWarps.getInstance(), location, task -> runnable.run());
    }

    public static void runLaterAt(Location location, Runnable runnable, long delay) {
        Bukkit.getRegionScheduler().runDelayed(AxPlayerWarps.getInstance(), location, task -> runnable.run(), Math.max(1, delay));
    }

    /**
     * Runs on the entity's thread; nothing happens if the entity was removed first.
     */
    public static void run(Entity entity, Runnable runnable) {
        entity.getScheduler().run(AxPlayerWarps.getInstance(), task -> runnable.run(), null);
    }

    public static void runLaterAsync(Runnable runnable, long delay) {
        Bukkit.getAsyncScheduler().runDelayed(AxPlayerWarps.getInstance(), task -> runnable.run(), delay * 50, TimeUnit.MILLISECONDS);
    }

    public static void runAsyncTimer(Consumer<ScheduledTask> task, long delay, long period) {
        Bukkit.getAsyncScheduler().runAtFixedRate(AxPlayerWarps.getInstance(), task, Math.max(1, delay * 50), period * 50, TimeUnit.MILLISECONDS);
    }
}
