package com.artillexstudios.axplayerwarps.hooks.protection;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlotSquaredHook implements ProtectionHook {

    @Override
    public boolean canBreak(@NotNull Player player, @NotNull Location location) {
        com.plotsquared.core.location.Location plotLocation = com.plotsquared.core.location.Location.at(
                location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()
        );
        PlotArea area = PlotSquared.get().getPlotAreaManager().getPlotArea(plotLocation);
        if (area == null) return true; // not a plot world
        Plot plot = area.getPlotAbs(plotLocation);
        if (plot == null) return false; // road
        return plot.isOwner(player.getUniqueId()) || plot.isAdded(player.getUniqueId());
    }
}
