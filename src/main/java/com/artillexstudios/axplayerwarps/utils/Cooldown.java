package com.artillexstudios.axplayerwarps.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cooldown<T> {
    private final Map<T, Long> expiries = new ConcurrentHashMap<>();

    public void addCooldown(T key, long millis) {
        expiries.put(key, System.currentTimeMillis() + millis);
    }

    public boolean hasCooldown(T key) {
        Long expiry = expiries.get(key);
        if (expiry == null) return false;
        if (expiry > System.currentTimeMillis()) return true;
        expiries.remove(key, expiry);
        return false;
    }

    public void remove(T key) {
        expiries.remove(key);
    }
}
