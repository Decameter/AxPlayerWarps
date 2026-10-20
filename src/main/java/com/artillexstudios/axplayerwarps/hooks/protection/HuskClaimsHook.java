package com.artillexstudios.axplayerwarps.hooks.protection;

import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.libraries.cloplib.operation.OperationType;
import net.william278.huskclaims.position.Position;
import net.william278.huskclaims.user.OnlineUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HuskClaimsHook implements ProtectionHook {
    private final BukkitHuskClaimsAPI api = BukkitHuskClaimsAPI.getInstance();

    @Override
    public boolean canBreak(@NotNull Player player, @NotNull Location location) {
        OnlineUser user = api.getOnlineUser(player.getUniqueId());
        if (user == null) return true;
        Position position = api.getPosition(location);
        if (position == null) return true;
        return api.isOperationAllowed(user, OperationType.BLOCK_BREAK, position);
    }
}
