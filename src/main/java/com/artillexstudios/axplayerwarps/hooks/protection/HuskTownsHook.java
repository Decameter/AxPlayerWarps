package com.artillexstudios.axplayerwarps.hooks.protection;

import net.william278.husktowns.api.BukkitHuskTownsAPI;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.libraries.cloplib.operation.OperationType;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HuskTownsHook implements ProtectionHook {
    private final BukkitHuskTownsAPI api = BukkitHuskTownsAPI.getInstance();

    @Override
    public boolean canBreak(@NotNull Player player, @NotNull Location location) {
        OnlineUser user = api.getOnlineUser(player);
        Position position = api.getPosition(location);
        return api.isOperationAllowed(user, OperationType.BLOCK_BREAK, position);
    }
}
