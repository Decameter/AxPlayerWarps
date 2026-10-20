package com.artillexstudios.axplayerwarps.sorting;

import com.artillexstudios.axplayerwarps.enums.Sorting;
import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.artillexstudios.axplayerwarps.AxPlayerWarps.CONFIG;

public class SortingManager {
    private static final LinkedList<Sort> enabledSorting = new LinkedList<>();

    public static void reload() {
        enabledSorting.clear();

        LinkedList<Sort> sorts = new LinkedList<>();
        Sort def = null;
        for (String raw : CONFIG.getSection("sorting").getRoutesAsStrings(false)) {
            Section section = CONFIG.getSection("sorting." + raw);

            Section forwards = section.getSection("forwards");
            if (forwards.getBoolean("enabled")) {
                Sort sort = new Sort(forwards.getString("name"), Sorting.valueOf(raw.toUpperCase(Locale.ENGLISH)), false);
                sorts.add(sort);
                if (forwards.getBoolean("default")) def = sort;
            }
            Section backwards = section.getSection("backwards");
            if (backwards.getBoolean("enabled")) {
                Sort sort = new Sort(backwards.getString("name"), Sorting.valueOf(raw.toUpperCase(Locale.ENGLISH)), true);
                sorts.add(sort);
                if (backwards.getBoolean("default")) def = sort;
            }
        }

        boolean go = false;
        for (Iterator<Sort> it = sorts.iterator(); it.hasNext(); ) {
            Sort sort = it.next();
            if (go) {
                enabledSorting.add(sort);
                it.remove();
                continue;
            }
            if (sort != def) continue;
            go = true;
            enabledSorting.add(sort);
            it.remove();

        }
        enabledSorting.addAll(sorts);
    }

    public static List<Sort> getEnabledSorting() {
        return enabledSorting;
    }
}
