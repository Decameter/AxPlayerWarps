package com.artillexstudios.axplayerwarps.hooks.protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Checks claims or regions before a warp is created. Must return true for players with the plugin's bypass.
 */
public interface ProtectionHook {

    boolean canBreak(@NotNull Player player, @NotNull Location location);
}
