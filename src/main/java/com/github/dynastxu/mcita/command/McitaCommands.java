package com.github.dynastxu.mcita.command;

import com.github.dynastxu.mcita.McitaRegistries;
import com.github.dynastxu.mcita.core.wand.WandTemplate;
import com.github.dynastxu.mcita.item.WandFactory;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Small test/debug command for preset wands. It is intentionally only a way to
 * obtain an already-created instance; it never initializes a bare wand.
 */
public final class McitaCommands {
    private static final SimpleCommandExceptionType INVALID_ID =
            new SimpleCommandExceptionType(Component.translatable("commands.mcita.wand.invalid"));

    private McitaCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mcita")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("wand")
                        .then(Commands.argument("preset", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    context.getSource().getServer().registryAccess()
                                            .lookupOrThrow(McitaRegistries.WAND)
                                            .listElements()
                                            .forEach(holder -> builder.suggest(
                                                    holder.key().identifier().toString()));
                                    return builder.buildFuture();
                                })
                                .executes(context -> givePreset(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "preset"))))));
    }

    private static int givePreset(CommandSourceStack source, String id) throws CommandSyntaxException {
        Identifier location = Identifier.tryParse(id.contains(":") ? id : "mcita:" + id);
        if (location == null) {
            throw INVALID_ID.create();
        }

        ResourceKey<WandTemplate> key = ResourceKey.create(McitaRegistries.WAND, location);
        Holder<WandTemplate> template = source.getServer().registryAccess()
                .lookupOrThrow(McitaRegistries.WAND)
                .get(key)
                .orElseThrow(() -> new SimpleCommandExceptionType(
                        Component.translatable("commands.mcita.wand.unknown", id)).create());

        ServerPlayer player = source.getPlayerOrException();
        if (!player.getInventory().add(WandFactory.preset(template))) {
            player.drop(WandFactory.preset(template), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
