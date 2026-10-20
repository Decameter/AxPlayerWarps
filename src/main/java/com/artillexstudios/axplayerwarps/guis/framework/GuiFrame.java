package com.artillexstudios.axplayerwarps.guis.framework;

import com.artillexstudios.axplayerwarps.AxPlayerWarps;
import com.artillexstudios.axplayerwarps.config.Config;
import com.artillexstudios.axplayerwarps.guis.framework.actions.GuiActions;
import com.artillexstudios.axplayerwarps.placeholders.PlaceholderHandler;
import com.artillexstudios.axplayerwarps.utils.ItemBuilder;
import com.artillexstudios.axplayerwarps.utils.NumberUtils;
import com.artillexstudios.axplayerwarps.utils.StringUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.triumphteam.gui.guis.BaseGui;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A gui built from a config file. Every section with a slot and a material/type becomes an item;
 * items may have "conditions" and "actions" (see {@link GuiActions}).
 */
public abstract class GuiFrame<T extends BaseGui> {
    protected T gui;
    protected final Section section;
    protected final Player player;
    protected final int updateTicks;
    private final Replacements globalReplacements = new Replacements();
    private final List<Object> placeholderParameters = new ArrayList<>();
    private Supplier<String> titleSupplier;
    private String currentTitle = "";
    private ScheduledTask updateTask;

    public GuiFrame(int updateTicks, Config config, Player player) {
        this.section = config.getBackingDocument();
        this.player = player;
        this.updateTicks = updateTicks;
    }

    public void setGui(T gui, Supplier<String> titleSupplier) {
        this.gui = gui;
        this.titleSupplier = titleSupplier;
        if (updateTicks > 0) {
            gui.setOpenGuiAction(event -> startUpdating());
            gui.setCloseGuiAction(event -> stopUpdating());
        }

        updateStaticItems();
        updateTitle();
    }

    @NotNull
    public Section getSection() {
        return section;
    }

    public T getGui() {
        return gui;
    }

    public void open() {
        gui.open(player);
    }

    /**
     * Called by the auto updater and the [REFRESH] action's callers, on the player's thread.
     */
    public void update() {
        updateTitle();
        gui.update();
    }

    private void startUpdating() {
        if (updateTask != null) return;
        updateTask = player.getScheduler().runAtFixedRate(AxPlayerWarps.getInstance(), task -> {
            if (player.getOpenInventory().getTopInventory().getHolder(false) != gui) {
                stopUpdating();
                return;
            }
            update();
        }, null, updateTicks, updateTicks);
    }

    private void stopUpdating() {
        if (updateTask == null) return;
        updateTask.cancel();
        updateTask = null;
    }

    public void updateStaticItems() {
        gui.clearItems();
        for (String route : section.getRoutesAsStrings(false)) {
            createItem(route);
        }
    }

    /**
     * Sets the title from the title supplier if it changed. Players viewing the gui see it reopen with the new title.
     */
    public boolean updateTitle() {
        if (titleSupplier == null) return false;
        String newTitle = titleSupplier.get();
        if (currentTitle.equals(newTitle)) return false;
        currentTitle = newTitle;
        gui.updateTitle(StringUtils.format(newTitle));
        return true;
    }

    public void addPlaceholderParameter(Object parameter) {
        placeholderParameters.add(parameter);
    }

    public void addReplacement(Replacements replacements) {
        globalReplacements.and(replacements);
    }

    /**
     * Applies the gui's replacements and the plugin's placeholders, with the gui's parameters, the viewer and the extra objects.
     */
    public String parseText(String original, Object... parameters) {
        original = globalReplacements.apply(original);
        List<Object> list = new ArrayList<>(placeholderParameters.size() + parameters.length + 1);
        list.addAll(Arrays.asList(parameters));
        list.addAll(placeholderParameters);
        list.add(player);
        return PlaceholderHandler.parse(original, list.toArray());
    }

    public List<String> parseText(List<String> originals, Object... parameters) {
        List<String> parsed = new ArrayList<>(originals.size());
        for (String original : originals) {
            parsed.add(parseText(original, parameters));
        }
        return parsed;
    }

    // also applies PlaceholderAPI, like item texts
    private String parseItemText(String original, Replacements replacements) {
        original = replacements.apply(original);
        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            original = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, original);
        }
        return parseText(original);
    }

    /**
     * Slots can be numbers, ranges (10-16), columns (10|28, every row from 10 to 28) or "border".
     */
    public List<Integer> getSlots(List<String> list) {
        List<Integer> slots = new ArrayList<>();
        int rows = gui.getRows();
        for (String str : list) {
            if (NumberUtils.isInt(str)) {
                slots.add(Integer.parseInt(str));
            } else if (str.contains("-")) {
                String[] split = str.split("-");
                for (int i = Integer.parseInt(split[0].trim()); i <= Integer.parseInt(split[1].trim()); i++) slots.add(i);
            } else if (str.contains("|")) {
                String[] split = str.split("\\|");
                for (int i = Integer.parseInt(split[0].trim()); i <= Integer.parseInt(split[1].trim()); i += 9) slots.add(i);
            } else if (str.equals("border")) {
                for (int i = 0; i < rows * 9; i++) {
                    if (i < 9 || i >= 9 * (rows - 1) || i % 9 == 0 || i % 9 == 8) slots.add(i);
                }
            }
        }
        return slots;
    }

    protected ItemStack buildItem(@NotNull String route, Replacements replacements) {
        Section itemSection = section.getSection(route);
        Replacements all = new Replacements().and(globalReplacements).and(replacements);

        ItemBuilder builder = ItemBuilder.create(itemSection);
        itemSection.getOptionalString("material").or(() -> itemSection.getOptionalString("type")).ifPresent(material -> {
            String parsed = parseItemText(material, all);
            if (!parsed.equals(material)) {
                Material type = Material.matchMaterial(parsed);
                if (type != null && type.isItem()) builder.setType(type);
            }
        });

        String name = itemSection.getString("name");
        if (name != null) {
            name = parseItemText(name, all);
            builder.setName(name);
        }

        List<String> lore = itemSection.getStringList("lore");
        if (!lore.isEmpty()) {
            lore = new ArrayList<>(lore);
            lore.replaceAll(line -> parseItemText(line, all));
            builder.setLore(lore);
        }

        String amount = itemSection.getString("amount");
        if (amount != null) {
            amount = parseItemText(amount, all);
            builder.setAmount(NumberUtils.isInt(amount) ? Integer.parseInt(amount) : 1);
        }

        if (name != null && name.isBlank() && (lore.isEmpty() || lore.getFirst().isBlank())) builder.hideTooltip();

        ItemStack item = builder.get();
        if (player != null && itemSection.getString("texture") == null && item.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
            item.setItemMeta(skullMeta);
        }
        return item;
    }

    protected AxGuiItem createItem(@NotNull String route) {
        return createItem(route, null, null, new Replacements(), List.of());
    }

    protected AxGuiItem createItem(@NotNull String route, @Nullable Consumer<InventoryClickEvent> action) {
        return createItem(route, null, action, new Replacements(), List.of());
    }

    protected AxGuiItem createItem(@NotNull String route, @Nullable Consumer<InventoryClickEvent> action, List<Integer> overrideSlots) {
        return createItem(route, null, action, new Replacements(), overrideSlots);
    }

    protected AxGuiItem createItem(@NotNull String route, @Nullable ItemStack item, @Nullable Consumer<InventoryClickEvent> action) {
        return createItem(route, item, action, new Replacements(), List.of());
    }

    /**
     * Creates the item of a section, or returns null if a condition fails or it has no material or slot.
     * The section's actions run on click, then the given action. A custom action is expected to run the
     * section's actions itself, so only one of them runs.
     */
    protected AxGuiItem createItem(@NotNull String route, @Nullable ItemStack item, @Nullable Consumer<InventoryClickEvent> action, Replacements replacements, List<Integer> overrideSlots) {
        Section itemSection = section.getSection(route);
        if (itemSection == null) return null;

        for (String condition : itemSection.getStringList("conditions")) {
            try {
                if (!SimpleExpression.eval(parseItemText(condition, replacements))) return null;
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().warning("[AxPlayerWarps] " + ex.getMessage());
            }
        }

        if (itemSection.getString("material") == null && itemSection.getString("type") == null) return null;

        AxGuiItem guiItem = new AxGuiItem(item == null ? buildItem(route, replacements) : item, event -> {
            if (action == null) {
                GuiActions.run(player, this, event, itemSection.getStringList("actions"));
            } else {
                action.accept(event);
            }
        });

        if (overrideSlots.isEmpty()) {
            List<String> slots = itemSection.getStringList("slot");
            String slot = itemSection.getString("slot");
            if (slots.isEmpty() && slot == null) return null;
            gui.setItem(getSlots(slots.isEmpty() ? List.of(slot) : slots), guiItem);
        } else {
            gui.setItem(overrideSlots, guiItem);
        }
        return guiItem;
    }
}
