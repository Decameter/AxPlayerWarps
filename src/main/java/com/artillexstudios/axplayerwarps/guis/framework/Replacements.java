package com.artillexstudios.axplayerwarps.guis.framework;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Plain text replacements, evaluated every time they are applied.
 */
public class Replacements {
    private final Map<String, Supplier<String>> replacements = new LinkedHashMap<>();

    public Replacements() {
    }

    public Replacements(String key, Supplier<String> value) {
        and(key, value);
    }

    public Replacements(String key, Object value) {
        and(key, value);
    }

    public Replacements and(String key, Supplier<String> value) {
        replacements.put(key, value);
        return this;
    }

    public Replacements and(String key, Object value) {
        String string = String.valueOf(value);
        replacements.put(key, () -> string);
        return this;
    }

    public Replacements and(Replacements another) {
        replacements.putAll(another.replacements);
        return this;
    }

    public String apply(String original) {
        for (Map.Entry<String, Supplier<String>> entry : replacements.entrySet()) {
            if (!original.contains(entry.getKey())) continue;
            original = original.replace(entry.getKey(), entry.getValue().get());
        }
        return original;
    }

    public List<String> apply(List<String> originals) {
        List<String> list = new ArrayList<>(originals.size());
        for (String original : originals) {
            list.add(apply(original));
        }
        return list;
    }
}
