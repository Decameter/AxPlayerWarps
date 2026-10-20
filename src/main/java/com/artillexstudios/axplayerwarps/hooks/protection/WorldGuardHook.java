package com.artillexstudios.axplayerwarps.hooks.protection;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldGuardHook implements ProtectionHook {
    private final WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();

    @Override
    public boolean canBreak(@NotNull Player player, @NotNull Location location) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        if (platform.getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(location.getWorld()))) return true;

        RegionQuery query = platform.getRegionContainer().createQuery();
        com.sk89q.worldedit.util.Location adapted = BukkitAdapter.adapt(location);
        return query.testState(adapted, localPlayer, Flags.BLOCK_BREAK) || query.testState(adapted, localPlayer, Flags.BUILD);
    }
}
