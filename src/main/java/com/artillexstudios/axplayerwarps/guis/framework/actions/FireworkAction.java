package com.artillexstudios.axplayerwarps.guis.framework.actions;

import com.artillexstudios.axplayerwarps.AxPlayerWarps;
import com.artillexstudios.axplayerwarps.guis.framework.GuiFrame;
import com.artillexstudios.axplayerwarps.utils.Scheduler;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

/**
 * [firework] #RRGGBB|type, a harmless firework that explodes right away at the player.
 */
public class FireworkAction implements Action, Listener {
    private final NamespacedKey key = new NamespacedKey(AxPlayerWarps.getInstance(), "gui_firework");
    private boolean registered = false;

    @Override
    public void run(Player player, GuiFrame<?> gui, InventoryClickEvent event, String arguments) {
        if (!registered) {
            registered = true;
            AxPlayerWarps.getInstance().getServer().getPluginManager().registerEvents(this, AxPlayerWarps.getInstance());
        }

        String[] split = arguments.split("\\|");
        Color color = Color.fromRGB(Integer.parseInt(split[0].replace("#", ""), 16));
        FireworkEffect.Type type = split.length > 1 ? FireworkEffect.Type.valueOf(split[1].toUpperCase(Locale.ENGLISH)) : FireworkEffect.Type.BALL;

        Scheduler.run(player, () -> player.getWorld().spawn(player.getLocation(), Firework.class, firework -> {
            firework.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 0);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().with(type).withColor(color).build());
            meta.setPower(0);
            firework.setFireworkMeta(meta);
        }).detonate());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Firework firework)) return;
        if (!firework.getPersistentDataContainer().has(key)) return;
        event.setCancelled(true);
    }
}
