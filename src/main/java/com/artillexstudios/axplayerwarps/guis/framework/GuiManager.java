package com.artillexstudios.axplayerwarps.guis.framework;

import com.artillexstudios.axplayerwarps.utils.Cooldown;
import com.artillexstudios.axplayerwarps.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Menus that can be opened by name with the [MENU] action, and the shared click cooldown.
 */
public class GuiManager {
    private static final Map<String, Function<Player, GuiFrame<?>>> guiTypes = new HashMap<>();
    private static final Cooldown<HumanEntity> cooldown = new Cooldown<>();
    private static long cooldownMillis = 0;

    public static void registerGuiType(String name, Function<Player, GuiFrame<?>> factory) {
        guiTypes.put(name.toLowerCase(Locale.ENGLISH), factory);
    }

    public static void open(Player player, String name) {
        name = name.replace(".yml", "").replace(".yaml", "");
        Function<Player, GuiFrame<?>> factory = guiTypes.get(name.toLowerCase(Locale.ENGLISH));
        if (factory == null) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.format("&#FF0000[AxPlayerWarps] Menu not found: " + name + "!"));
            return;
        }
        factory.apply(player).open();
    }

    public static void setCooldownMillis(long cooldownMillis) {
        GuiManager.cooldownMillis = cooldownMillis;
    }

    /**
     * @return true if the player clicked too recently, otherwise starts a new cooldown
     */
    public static boolean isOnCooldown(HumanEntity player) {
        if (cooldownMillis <= 0) return false;
        if (cooldown.hasCooldown(player)) return true;
        cooldown.addCooldown(player, cooldownMillis);
        return false;
    }
}
