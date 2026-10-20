package com.artillexstudios.axplayerwarps.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread pool used to build gui items off the main thread.
 */
public final class AsyncUtils {
    private static ExecutorService executor;

    private AsyncUtils() {
    }

    public static void start(int threads) {
        AtomicInteger counter = new AtomicInteger();
        executor = Executors.newFixedThreadPool(Math.max(1, threads), runnable -> {
            Thread thread = new Thread(runnable, "AxPlayerWarps-gui-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    public static void submit(Runnable runnable) {
        executor.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public static void stop() {
        if (executor != null) executor.shutdownNow();
    }
}
