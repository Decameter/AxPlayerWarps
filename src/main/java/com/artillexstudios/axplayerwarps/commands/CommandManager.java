package com.artillexstudios.axplayerwarps.commands;

import com.artillexstudios.axplayerwarps.AxPlayerWarps;
import com.artillexstudios.axplayerwarps.commands.subcommands.Admin;
import com.artillexstudios.axplayerwarps.commands.subcommands.Converter;
import com.artillexstudios.axplayerwarps.commands.subcommands.Create;
import com.artillexstudios.axplayerwarps.commands.subcommands.Delete;
import com.artillexstudios.axplayerwarps.commands.subcommands.Edit;
import com.artillexstudios.axplayerwarps.commands.subcommands.Help;
import com.artillexstudios.axplayerwarps.commands.subcommands.Info;
import com.artillexstudios.axplayerwarps.commands.subcommands.Open;
import com.artillexstudios.axplayerwarps.commands.subcommands.Reload;
import com.artillexstudios.axplayerwarps.enums.Converters;
import com.artillexstudios.axplayerwarps.warps.Warp;
import com.artillexstudios.axplayerwarps.warps.WarpManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.artillexstudios.axplayerwarps.AxPlayerWarps.CONFIG;
import static com.artillexstudios.axplayerwarps.AxPlayerWarps.MESSAGEUTILS;

/**
 * Registers the commands through Paper's Brigadier API. Aliases are read once, on startup.
 * Warp names are read until the end of the command, so they may contain spaces.
 */
public class CommandManager {
    private static final String WARP = "warp";
    private static final String PLAYER = "player";

    public static void load() {
        List<String> main = CONFIG.getStringList("main-command-aliases");
        List<String> admin = CONFIG.getStringList("admin-command-aliases");

        AxPlayerWarps.getInstance().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            if (!main.isEmpty()) {
                event.registrar().register(buildMain(main.getFirst()), "Player warps", main.subList(1, main.size()));
            }
            if (!admin.isEmpty()) {
                event.registrar().register(buildAdmin(admin.getFirst()), "Player warps admin commands", admin.subList(1, admin.size()));
            }
        });
    }

    private static LiteralCommandNode<CommandSourceStack> buildMain(String name) {
        return Commands.literal(name)
                .executes(ctx -> run(ctx, "axplayerwarps.open", sender -> Open.INSTANCE.execute(sender, (Player) null)))
                .then(warpArgument(StringArgumentType.greedyString(), false)
                        .requires(permission("axplayerwarps.open").and(permission("axplayerwarps.use")))
                        .executes(ctx -> withWarp(ctx, (sender, warp) -> Open.INSTANCE.execute(sender, warp))))
                .then(literal("help", "axplayerwarps.help")
                        .executes(ctx -> run(ctx, Help.INSTANCE::execute)))
                .then(literal("open", "axplayerwarps.open")
                        .executes(ctx -> run(ctx, sender -> Open.INSTANCE.execute(sender, (Player) null)))
                        .then(playerArgument()
                                .requires(permission("axplayerwarps.open.other"))
                                .executes(ctx -> run(ctx, sender -> {
                                    Player target = resolveOnlinePlayer(ctx);
                                    if (target != null) Open.INSTANCE.execute(sender, target);
                                }))))
                .then(warpCommand("warp", "axplayerwarps.use", false, (player, warp) -> warp.teleportPlayer(player)))
                .then(warpCommand("go", "axplayerwarps.use", false, (player, warp) -> warp.teleportPlayer(player)))
                .then(createCommand("create"))
                .then(createCommand("set"))
                .then(warpCommand("delete", "axplayerwarps.delete", true, Delete.INSTANCE::execute))
                .then(warpCommand("edit", "axplayerwarps.edit", true, Edit.INSTANCE::execute))
                .then(warpCommand("settings", "axplayerwarps.edit", true, Edit.INSTANCE::execute))
                .then(literal("info", "axplayerwarps.info")
                        .then(warpArgument(StringArgumentType.greedyString(), false)
                                .executes(ctx -> withWarp(ctx, Info.INSTANCE::execute))))
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> buildAdmin(String name) {
        return Commands.literal(name)
                .requires(source -> {
                    CommandSender sender = source.getSender();
                    for (String permission : List.of("help", "reload", "delete", "setowner", "converter")) {
                        if (sender.hasPermission("axplayerwarps.admin." + permission)) return true;
                    }
                    return false;
                })
                .executes(ctx -> run(ctx, "axplayerwarps.admin.help", Admin.INSTANCE::help))
                .then(literal("help", "axplayerwarps.admin.help")
                        .executes(ctx -> run(ctx, Admin.INSTANCE::help)))
                .then(literal("reload", "axplayerwarps.admin.reload")
                        .executes(ctx -> run(ctx, Reload.INSTANCE::execute)))
                .then(literal("delete", "axplayerwarps.admin.delete")
                        .then(warpArgument(StringArgumentType.greedyString(), false)
                                .executes(ctx -> withWarp(ctx, Admin.INSTANCE::delete))))
                .then(literal("deleteid", "axplayerwarps.admin.delete")
                        .then(Commands.argument("id", IntegerArgumentType.integer())
                                .executes(ctx -> run(ctx, sender -> Admin.INSTANCE.deleteId(sender, IntegerArgumentType.getInteger(ctx, "id"))))))
                .then(literal("setowner", "axplayerwarps.admin.setowner")
                        // quotable, so names with spaces can be given before the player
                        .then(warpArgument(StringArgumentType.string(), false)
                                .then(playerArgument()
                                        .executes(ctx -> withWarp(ctx, (sender, warp) -> {
                                            String playerName = StringArgumentType.getString(ctx, PLAYER);
                                            OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(playerName);
                                            if (player == null) {
                                                MESSAGEUTILS.sendLang(sender, "commands.invalid-player", Map.of("%player%", playerName));
                                                return;
                                            }
                                            Admin.INSTANCE.setOwner(sender, warp, player);
                                        })))))
                .then(literal("converter", "axplayerwarps.admin.converter")
                        .then(Commands.argument("converter", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (Converters converter : Converters.values()) builder.suggest(converter.name());
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> run(ctx, sender -> {
                                    String value = StringArgumentType.getString(ctx, "converter");
                                    try {
                                        Converter.INSTANCE.execute(sender, Converters.valueOf(value.toUpperCase(Locale.ENGLISH)));
                                    } catch (IllegalArgumentException ex) {
                                        MESSAGEUTILS.sendLang(sender, "commands.invalid-value", Map.of("%value%", value));
                                    }
                                }))))
                .build();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> literal(String name, String permission) {
        return Commands.literal(name).requires(permission(permission));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createCommand(String name) {
        return literal(name, "axplayerwarps.create")
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> asPlayer(ctx, player -> Create.INSTANCE.execute(player, StringArgumentType.getString(ctx, "name")))));
    }

    /**
     * A player only subcommand taking a warp, optionally limited to (and suggesting) the player's own warps.
     */
    private static LiteralArgumentBuilder<CommandSourceStack> warpCommand(String name, String permission, boolean ownWarps, BiConsumer<Player, Warp> action) {
        return literal(name, permission)
                .then(warpArgument(StringArgumentType.greedyString(), ownWarps)
                        .executes(ctx -> asPlayer(ctx, player -> {
                            Warp warp = resolveWarp(ctx);
                            if (warp != null) action.accept(player, warp);
                        })));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> warpArgument(ArgumentType<String> type, boolean ownWarps) {
        return Commands.argument(WARP, type)
                .suggests((ctx, builder) -> {
                    String remaining = builder.getRemainingLowerCase();
                    CommandSender sender = ctx.getSource().getSender();
                    Collection<Warp> warps = ownWarps && sender instanceof Player player ? WarpManager.getWarps(player) : List.copyOf(WarpManager.getWarps());
                    for (Warp warp : warps) {
                        if (!warp.getName().toLowerCase(Locale.ENGLISH).startsWith(remaining)) continue;
                        // quotable arguments need quotes around names with spaces
                        boolean quote = type instanceof StringArgumentType string && string.getType() == StringArgumentType.StringType.QUOTABLE_PHRASE && warp.getName().contains(" ");
                        builder.suggest(quote ? "\"" + warp.getName() + "\"" : warp.getName());
                    }
                    return builder.buildFuture();
                });
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> playerArgument() {
        return Commands.argument(PLAYER, StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    String remaining = builder.getRemainingLowerCase();
                    CommandSender sender = ctx.getSource().getSender();
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (sender instanceof Player self && !self.canSee(online)) continue;
                        if (online.getName().toLowerCase(Locale.ENGLISH).startsWith(remaining)) builder.suggest(online.getName());
                    }
                    return builder.buildFuture();
                });
    }

    private static Predicate<CommandSourceStack> permission(String permission) {
        return source -> source.getSender().hasPermission(permission);
    }

    private static int run(CommandContext<CommandSourceStack> ctx, Consumer<CommandSender> action) {
        action.accept(ctx.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }

    // for nodes that can not use requires(), like the root node that also holds other subcommands
    private static int run(CommandContext<CommandSourceStack> ctx, String permission, Consumer<CommandSender> action) {
        CommandSender sender = ctx.getSource().getSender();
        if (!sender.hasPermission(permission)) {
            MESSAGEUTILS.sendLang(sender, "commands.no-permission");
            return Command.SINGLE_SUCCESS;
        }
        return run(ctx, action);
    }

    private static int asPlayer(CommandContext<CommandSourceStack> ctx, Consumer<Player> action) {
        if (ctx.getSource().getSender() instanceof Player player) {
            action.accept(player);
        } else {
            MESSAGEUTILS.sendLang(ctx.getSource().getSender(), "commands.player-only");
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int withWarp(CommandContext<CommandSourceStack> ctx, BiConsumer<CommandSender, Warp> action) {
        return run(ctx, sender -> {
            Warp warp = resolveWarp(ctx);
            if (warp != null) action.accept(sender, warp);
        });
    }

    @Nullable
    private static Warp resolveWarp(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, WARP);
        Warp warp = WarpManager.getWarp(name);
        if (warp == null) MESSAGEUTILS.sendLang(ctx.getSource().getSender(), "errors.not-found", Map.of("%warp%", name));
        return warp;
    }

    @Nullable
    private static Player resolveOnlinePlayer(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, PLAYER);
        Player target = Bukkit.getPlayerExact(name);
        CommandSender sender = ctx.getSource().getSender();
        if (target == null || (sender instanceof Player self && !self.canSee(target))) {
            MESSAGEUTILS.sendLang(sender, "commands.invalid-player", Map.of("%player%", name));
            return null;
        }
        return target;
    }
}
