package com.artillexstudios.axplayerwarps.hooks.protection;

import com.artillexstudios.axplayerwarps.AxPlayerWarps;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.type.Flags;
import me.angeschossen.lands.api.land.LandWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LandsHook implements ProtectionHook {
    private final LandsIntegration api = LandsIntegration.of(AxPlayerWarps.getInstance());

    @Override
    public boolean canBreak(@NotNull Player player, @NotNull Location location) {
        LandWorld world = api.getWorld(location.getWorld());
        if (world == null) return true;
        return world.hasRoleFlag(player.getUniqueId(), location, Flags.BLOCK_BREAK);
    }
}
