package com.artillexstudios.axplayerwarps.hooks.protection;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SuperiorSkyblockHook implements ProtectionHook {

    @Override
    public boolean canBreak(@NotNull Player player, @NotNull Location location) {
        Island island = SuperiorSkyblockAPI.getIslandAt(location);
        if (island == null) return true;
        return island.hasPermission(SuperiorSkyblockAPI.getPlayer(player), IslandPrivilege.getByName("BREAK"));
    }
}
