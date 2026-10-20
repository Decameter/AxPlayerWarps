package com.artillexstudios.axplayerwarps.hooks.protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;

public class BentoBoxHook implements ProtectionHook {

    @Override
    public boolean canBreak(@NotNull Player player, @NotNull Location location) {
        Island island = BentoBox.getInstance().getIslandsManager().getIslandAt(location).orElse(null);
        if (island == null) return true;
        return island.isAllowed(User.getInstance(player), Flags.BREAK_BLOCKS);
    }
}
