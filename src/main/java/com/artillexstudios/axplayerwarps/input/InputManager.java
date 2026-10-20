package com.artillexstudios.axplayerwarps.input;

import com.artillexstudios.axplayerwarps.utils.ItemBuilder;
import com.artillexstudios.axplayerwarps.utils.StringUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.AnvilView;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static com.artillexstudios.axplayerwarps.AxPlayerWarps.INPUT;

/**
 * Asks a player for text with a sign, an anvil or the chat, as set in input.yml.
 * The consumer runs on the player's thread and gets an empty string if the player cancelled.
 */
public class InputManager {

    public static void getInput(Player player, String route, Consumer<String> consumer) {
        Section section = INPUT.getSection(route);
        switch (InputType.valueOf(section.getString("type").toUpperCase(Locale.ENGLISH))) {
            case SIGN -> openSign(player, section, consumer);
            case ANVIL -> openAnvil(player, section, consumer);
            case CHAT -> openChat(player, section, consumer);
        }
    }

    // a sign that only exists for the player, placed a few blocks below them so it is not in their way
    private static void openSign(Player player, Section section, Consumer<String> consumer) {
        player.closeInventory();
        Location location = player.getLocation().getBlock().getLocation();
        location.setY(Math.max(location.getWorld().getMinHeight(), location.getBlockY() - 4));

        Sign sign = (Sign) Material.OAK_SIGN.createBlockData().createBlockState();
        SignSide side = sign.getSide(Side.FRONT);
        List<String> lines = section.getStringList("sign");
        for (int i = 0; i < Math.min(4, lines.size()); i++) {
            side.line(i, StringUtils.format(lines.get(i)));
        }

        player.sendBlockChange(location, sign.getBlockData());
        player.sendBlockUpdate(location, sign);
        InputListener.getSignInputs().put(player.getUniqueId(), new InputListener.SignInput(Position.block(location), location, consumer));
        player.openVirtualSign(Position.block(location), Side.FRONT);
    }

    private static void openAnvil(Player player, Section section, Consumer<String> consumer) {
        Component title = StringUtils.format(section.getString("anvil.title", ""));
        AnvilView view = MenuType.ANVIL.create(player, title);
        view.getTopInventory().setItem(0, ItemBuilder.create(section.getSection("anvil.item")).get());
        InputListener.getAnvilInputs().put(player.getUniqueId(), new InputListener.AnvilInput(view, consumer));
        view.open();
    }

    private static void openChat(Player player, Section section, Consumer<String> consumer) {
        player.closeInventory();
        for (String line : section.getStringList("chat")) {
            player.sendMessage(StringUtils.format(line));
        }
        InputListener.getChatInputs().put(player.getUniqueId(), consumer);
    }
}
