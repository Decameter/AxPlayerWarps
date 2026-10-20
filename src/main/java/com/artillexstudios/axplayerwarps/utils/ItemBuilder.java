package com.artillexstudios.axplayerwarps.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Builds items from config sections. Supported keys: snbt, material/type, amount, name, lore, enchants,
 * item-flags, color, texture, glow, potion, unbreakable, item-model, tooltip-style, custom-model-data.
 */
public class ItemBuilder {
    private static final UUID NIL_UUID = new UUID(0, 0);
    private ItemStack stack;

    private ItemBuilder(ItemStack stack) {
        this.stack = stack;
    }

    @NotNull
    public static ItemBuilder create(@NotNull ItemStack stack) {
        return new ItemBuilder(stack.clone());
    }

    /**
     * Name, lore and amount are applied as they are written; format them first if they need placeholders.
     */
    @NotNull
    public static ItemBuilder create(@NotNull Section section) {
        ItemBuilder builder = new ItemBuilder(createBase(section));
        ItemStack stack = builder.stack;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return builder;

        meta.addItemFlags(getItemFlags(section.getStringList("item-flags")).toArray(ItemFlag[]::new));

        for (String enchant : section.getStringList("enchants")) {
            addEnchant(meta, enchant);
        }

        section.getOptionalString("name").ifPresent(name -> meta.displayName(StringUtils.format(name)));
        section.getOptionalStringList("lore").ifPresent(lore -> meta.lore(formatLore(lore)));

        section.getOptionalString("color").ifPresent(color -> {
            if (meta instanceof LeatherArmorMeta leather) leather.setColor(parseColor(color));
        });

        section.getOptionalString("texture").ifPresent(texture -> {
            if (meta instanceof SkullMeta skull) setTexture(skull, texture);
        });

        section.getOptionalBoolean("glow").ifPresent(glow -> {
            if (glow) meta.setEnchantmentGlintOverride(true);
        });

        section.getOptionalString("potion").ifPresent(potion -> {
            if (meta instanceof PotionMeta potionMeta) potionMeta.setBasePotionType(PotionType.valueOf(potion.toUpperCase(Locale.ENGLISH)));
        });

        section.getOptionalBoolean("unbreakable").ifPresent(meta::setUnbreakable);
        section.getOptionalString("item-model").ifPresent(model -> meta.setItemModel(NamespacedKey.fromString(model)));
        section.getOptionalString("tooltip-style").ifPresent(style -> meta.setTooltipStyle(NamespacedKey.fromString(style)));

        if (section.isInt("custom-model-data")) {
            CustomModelDataComponent component = meta.getCustomModelDataComponent();
            component.setFloats(List.of((float) section.getInt("custom-model-data")));
            meta.setCustomModelDataComponent(component);
        } else if (section.isSection("custom-model-data")) {
            meta.setCustomModelDataComponent(customModelData(meta, section.getSection("custom-model-data")));
        }

        stack.setItemMeta(meta);

        Object amount = section.get("amount");
        if (amount instanceof Number number) {
            builder.setAmount(number.intValue());
        } else if (amount instanceof String string && NumberUtils.isInt(string.trim())) {
            builder.setAmount(Integer.parseInt(string.trim()));
        }

        return builder;
    }

    public ItemBuilder setName(@NotNull String name) {
        stack.editMeta(meta -> meta.displayName(StringUtils.format(name)));
        return this;
    }

    public ItemBuilder setLore(@NotNull List<String> lore) {
        stack.editMeta(meta -> meta.lore(formatLore(lore)));
        return this;
    }

    public ItemBuilder setType(@NotNull Material material) {
        stack = stack.withType(material);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        stack.setAmount(Math.clamp(amount, 1, 99));
        return this;
    }

    public ItemBuilder setTextureValue(@NotNull String texture) {
        stack.editMeta(meta -> {
            if (meta instanceof SkullMeta skull) setTexture(skull, texture);
        });
        return this;
    }

    /**
     * Hides the whole tooltip, used for items with an empty name and lore.
     */
    public ItemBuilder hideTooltip() {
        stack.editMeta(meta -> meta.setHideTooltip(true));
        return this;
    }

    @NotNull
    public ItemStack get() {
        return stack;
    }

    private static ItemStack createBase(Section section) {
        String snbt = section.getString("snbt");
        if (snbt != null) {
            try {
                return Bukkit.getItemFactory().createItemStack(snbt);
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().warning("[AxPlayerWarps] Invalid snbt item: " + snbt);
            }
        }

        String type = section.getString("material", section.getString("type"));
        Material material = type == null ? null : Material.matchMaterial(type.toUpperCase(Locale.ENGLISH));
        return new ItemStack(material == null || !material.isItem() ? Material.BEDROCK : material);
    }

    private static List<Component> formatLore(List<String> lore) {
        List<Component> lines = new ArrayList<>(lore.size());
        for (String line : lore) {
            for (String part : line.replace("<br>", "\n").split("\n", -1)) {
                lines.add(StringUtils.format(part));
            }
        }
        return lines;
    }

    private static void addEnchant(ItemMeta meta, String enchant) {
        String[] split = enchant.split(":");
        if (split.length < 2) return;
        Enchantment enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.minecraft(split[0].toLowerCase(Locale.ENGLISH)));
        if (enchantment == null) return;
        try {
            meta.addEnchant(enchantment, Integer.parseInt(split[1]), true);
        } catch (NumberFormatException ignored) {
        }
    }

    private static List<ItemFlag> getItemFlags(List<String> flags) {
        List<ItemFlag> list = new ArrayList<>(flags.size());
        for (String flag : flags) {
            try {
                list.add(ItemFlag.valueOf(flag.toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return list;
    }

    private static Color parseColor(String color) {
        String[] rgb = color.replace(" ", "").split(",");
        return Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
    }

    private static void setTexture(SkullMeta meta, String texture) {
        if (texture.isBlank()) return;
        PlayerProfile profile = Bukkit.createProfile(NIL_UUID, "axplayerwarps");
        profile.setProperty(new ProfileProperty("textures", texture));
        meta.setPlayerProfile(profile);
    }

    private static CustomModelDataComponent customModelData(ItemMeta meta, Section section) {
        CustomModelDataComponent component = meta.getCustomModelDataComponent();
        List<Float> floats = new ArrayList<>();
        for (Object number : section.getList("floats", List.of())) {
            if (number instanceof Number num) floats.add(num.floatValue());
        }
        List<Boolean> flags = new ArrayList<>();
        for (Object flag : section.getList("flags", List.of())) {
            if (flag instanceof Boolean bool) flags.add(bool);
        }
        List<Color> colors = new ArrayList<>();
        for (String color : section.getStringList("colors")) {
            colors.add(parseColor(color));
        }
        component.setFloats(floats);
        component.setFlags(flags);
        component.setStrings(section.getStringList("strings"));
        component.setColors(colors);
        return component;
    }

    /**
     * Base64 skin texture of a player, or null if they have none (offline mode).
     */
    @Nullable
    public static String getTexture(@NotNull Player player) {
        for (ProfileProperty property : player.getPlayerProfile().getProperties()) {
            if (property.getName().equals("textures")) return property.getValue();
        }
        return null;
    }
}
