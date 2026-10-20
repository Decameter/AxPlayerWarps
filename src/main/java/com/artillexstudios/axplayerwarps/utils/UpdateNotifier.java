package com.artillexstudios.axplayerwarps.utils;

import com.artillexstudios.axplayerwarps.AxPlayerWarps;
import com.artillexstudios.axplayerwarps.config.Config;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;

import static java.time.temporal.ChronoUnit.SECONDS;

public class UpdateNotifier implements Listener {
    private static Config config;
    private static Config lang;
    private static boolean onJoin;
    private static String prefix;
    private static String updateNotifier;

    private final String current;
    private String latest = null;
    private boolean newest = true;

    public static void init(Config config, Config lang) {
        UpdateNotifier.config = config;
        UpdateNotifier.lang = lang;
        reload();
    }

    public static void reload() {
        onJoin = config.getBoolean("update-notifier.on-join", true);
        prefix = config.getString("prefix");
        updateNotifier = lang.getString("update-notifier");
    }

    public UpdateNotifier() {
        this.current = AxPlayerWarps.getInstance().getPluginMeta().getVersion();

        AxPlayerWarps.getInstance().getServer().getPluginManager().registerEvents(this, AxPlayerWarps.getInstance());

        long time = 30L * 60L * 20L;
        Scheduler.runAsyncTimer(t -> {
            this.latest = readVersion();
            this.newest = !isOutdated(current);

            if (latest == null || newest) return;
            Scheduler.runLaterAsync(() -> {
                Bukkit.getConsoleSender().sendMessage(getMessage());
            }, 50L);
            t.cancel();
        }, 0, time);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (latest == null || newest) return;
        if (!onJoin) return;
        if (!event.getPlayer().hasPermission(AxPlayerWarps.getInstance().getName().toLowerCase() + ".update-notify")) return;
        Scheduler.runLaterAsync(() -> {
            event.getPlayer().sendMessage(getMessage());
        }, 50L);
    }

    private Component getMessage() {
        HashMap<String, String> map = new HashMap<>();
        map.put("%current%", current);
        map.put("%latest%", latest);
        return StringUtils.format(String.format("%s %s", prefix, updateNotifier), map);
    }

    @Nullable
    private String readVersion() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://www.artillex-studios.com/api/v1/resource/%s/latest-version".formatted(AxPlayerWarps.getInstance().getName())))
                    .timeout(Duration.of(10, SECONDS))
                    .GET()
                    .build();

            HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;
            return response.body().toString();
        } catch (Exception ex) {
            return null;
        }
    }

    public String getLatest() {
        return latest;
    }

    public boolean isOutdated(String current) {
        if (latest == null) return false;
        String[] parts1 = latest.split("\\.");
        String[] parts2 = current.split("\\.");
        for (int i = 0; i < 3; i++) {
            int num1 = Integer.parseInt(parts1[i]);
            int num2 = Integer.parseInt(parts2[i]);
            if (num1 > num2) {
                return true;
            } else if (num1 < num2) {
                return false;
            }
        }
        return false;
    }
}