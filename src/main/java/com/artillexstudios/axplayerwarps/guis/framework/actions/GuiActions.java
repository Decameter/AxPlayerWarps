package com.artillexstudios.axplayerwarps.guis.framework.actions;

import com.artillexstudios.axplayerwarps.guis.framework.GuiFrame;
import com.artillexstudios.axplayerwarps.guis.framework.GuiManager;
import com.artillexstudios.axplayerwarps.utils.Scheduler;
import com.artillexstudios.axplayerwarps.utils.SoundUtils;
import com.artillexstudios.axplayerwarps.utils.StringUtils;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Built in actions: [console] and [player] commands, [message], [sound], [menu] (name or "close"),
 * [page] (next, previous or a number), [refresh] and [firework] (#RRGGBB|type).
 */
public class GuiActions {
    private static final Map<String, Action> ACTIONS = new HashMap<>();

    static {
        register("console", (player, gui, event, arguments) -> {
            String command = applyPlaceholders(player, arguments);
            Scheduler.run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        });
        register("player", (player, gui, event, arguments) -> {
            String command = applyPlaceholders(player, arguments);
            Scheduler.run(player, () -> player.performCommand(command));
        });
        register("message", (player, gui, event, arguments) -> player.sendMessage(StringUtils.format(applyPlaceholders(player, arguments))));
        register("sound", (player, gui, event, arguments) -> SoundUtils.playSound(player, arguments));
        register("menu", (player, gui, event, arguments) -> {
            if (arguments.equalsIgnoreCase("close")) {
                player.closeInventory();
                return;
            }
            GuiManager.open(player, arguments);
        });
        register("page", (player, gui, event, arguments) -> {
            if (!(gui.getGui() instanceof PaginatedGui paginated)) return;
            if (arguments.equalsIgnoreCase("previous")) {
                paginated.previous();
            } else if (arguments.equalsIgnoreCase("next")) {
                paginated.next();
            } else {
                try {
                    paginated.setPageNum(Integer.parseInt(arguments.trim()));
                    paginated.update();
                } catch (NumberFormatException ignored) {
                }
            }
            gui.updateTitle();
        });
        register("refresh", (player, gui, event, arguments) -> gui.open());
        register("firework", new FireworkAction());
    }

    public static void register(String id, Action action) {
        ACTIONS.put(id.toLowerCase(Locale.ENGLISH), action);
    }

    public static void run(Player player, GuiFrame<?> gui, InventoryClickEvent event, List<String> actions) {
        if (player == null) return;
        for (String rawAction : actions) {
            if (rawAction == null || rawAction.isBlank()) continue;
            int start = rawAction.indexOf('[');
            int end = rawAction.indexOf(']');
            if (start == -1 || end < start) continue;

            Action action = ACTIONS.get(rawAction.substring(start + 1, end).toLowerCase(Locale.ENGLISH));
            if (action == null) continue;
            action.run(player, gui, event, rawAction.substring(end + 1).trim());
        }
    }

    private static String applyPlaceholders(Player player, String text) {
        text = text.replace("%player%", player.getName());
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }
}
