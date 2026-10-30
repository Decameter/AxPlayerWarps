package com.artillexstudios.axplayerwarps.hooks.protection;

import net.leonemc.islands.Islands;
import net.leonemc.islands.islands.PlayerIsland;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Unlike the other hooks, warps outside of any island are refused: players may only set warps on islands they're a member of.
 */
public class IslandsHook implements ProtectionHook {

    @Override
    public boolean canBreak(@NotNull Player player, @NotNull Location location) {
        if (player.hasPermission("axplayerwarps.admin.bypass")) return true;
        PlayerIsland island = Islands.getInstance().getIslandManager().getIslandAt(location);
        return island != null && island.getMembers().containsKey(player.getUniqueId());
    }
}
