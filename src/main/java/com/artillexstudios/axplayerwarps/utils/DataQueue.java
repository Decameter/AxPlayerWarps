package com.artillexstudios.axplayerwarps.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Runs submitted tasks one after another on a single background thread, so database writes keep their order.
 */
public class DataQueue {
    private final ExecutorService executor;

    public DataQueue(String threadName) {
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, threadName);
            thread.setDaemon(true);
            return thread;
        });
    }

    public void submit(Runnable runnable) {
        executor.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    /**
     * Waits a little for queued writes to finish before shutting down.
     */
    public void stop() {
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
