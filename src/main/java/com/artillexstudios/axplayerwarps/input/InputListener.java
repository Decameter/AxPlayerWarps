package com.artillexstudios.axplayerwarps.input;

import com.artillexstudios.axplayerwarps.utils.Scheduler;
import io.papermc.paper.event.packet.UncheckedSignChangeEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.view.AnvilView;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.artillexstudios.axplayerwarps.AxPlayerWarps.INPUT;

public class InputListener implements Listener {
    private static final Map<UUID, Consumer<String>> chatInputs = new ConcurrentHashMap<>();
    private static final Map<UUID, SignInput> signInputs = new ConcurrentHashMap<>();
    private static final Map<UUID, AnvilInput> anvilInputs = new ConcurrentHashMap<>();

    record SignInput(BlockPosition position, Location location, Consumer<String> consumer) {}

    record AnvilInput(AnvilView view, Consumer<String> consumer) {}

    static Map<UUID, Consumer<String>> getChatInputs() {
        return chatInputs;
    }

    static Map<UUID, SignInput> getSignInputs() {
        return signInputs;
    }

    static Map<UUID, AnvilInput> getAnvilInputs() {
        return anvilInputs;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        chatInputs.remove(uuid);
        signInputs.remove(uuid);
        anvilInputs.remove(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Consumer<String> consumer = chatInputs.remove(event.getPlayer().getUniqueId());
        if (consumer == null) return;
        event.setCancelled(true);

        String message = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        String result = INPUT.getStringList("chat-cancel-words").contains(message) ? "" : message;
        complete(event.getPlayer(), consumer, result);
    }

    @EventHandler
    public void onSign(UncheckedSignChangeEvent event) {
        Player player = event.getPlayer();
        SignInput input = signInputs.get(player.getUniqueId());
        if (input == null || !input.position().equals(event.getEditedBlockPosition())) return;
        signInputs.remove(player.getUniqueId());
        event.setCancelled(true);

        // show the real block again
        player.sendBlockChange(input.location(), input.location().getBlock().getBlockData());
        String result = PlainTextComponentSerializer.plainText().serialize(event.lines().getFirst());
        complete(player, input.consumer(), result.isBlank() ? "" : result);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        AnvilInput input = anvilInputs.get(event.getWhoClicked().getUniqueId());
        if (input == null || event.getView() != input.view()) return;
        event.setCancelled(true);
        if (event.getRawSlot() != 2) return;

        anvilInputs.remove(event.getWhoClicked().getUniqueId());
        String result = input.view().getRenameText();
        input.view().getTopInventory().clear();
        event.getWhoClicked().closeInventory();
        complete((Player) event.getWhoClicked(), input.consumer(), result == null ? "" : result);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        AnvilInput input = anvilInputs.get(event.getPlayer().getUniqueId());
        if (input == null || event.getView() != input.view()) return;
        anvilInputs.remove(event.getPlayer().getUniqueId());
        input.view().getTopInventory().clear();
        complete((Player) event.getPlayer(), input.consumer(), "");
    }

    private static void complete(Player player, Consumer<String> consumer, String result) {
        Scheduler.run(player, () -> consumer.accept(result));
    }
}
