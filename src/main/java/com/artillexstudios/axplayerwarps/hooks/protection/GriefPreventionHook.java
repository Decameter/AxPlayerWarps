package com.artillexstudios.axplayerwarps.hooks.protection;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GriefPreventionHook implements ProtectionHook {

    @Override
    public boolean canBreak(@NotNull Player player, @NotNull Location location) {
        // returns the reason when the player is not allowed
        return GriefPrevention.instance.allowBreak(player, location.getBlock(), location) == null;
    }
}
