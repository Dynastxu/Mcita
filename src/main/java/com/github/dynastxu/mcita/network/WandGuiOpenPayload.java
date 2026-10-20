package com.github.dynastxu.mcita.network;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.item.WandItem;
import com.github.dynastxu.mcita.menu.WandMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import io.netty.buffer.ByteBuf;

/** Client -> server: open the wand GUI for the currently held wand. */
public record WandGuiOpenPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WandGuiOpenPayload> TYPE =
            new CustomPacketPayload.Type<>(Mcita.id("wand_gui_open"));
    public static final StreamCodec<ByteBuf, WandGuiOpenPayload> STREAM_CODEC =
            StreamCodec.unit(new WandGuiOpenPayload());

    public static void handle(final WandGuiOpenPayload payload, final @NonNull IPayloadContext context) {
        Player player = context.player();
        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof WandItem)
                || !mainHand.has(com.github.dynastxu.mcita.registry.ModDataComponents.WAND.get())) {
            return;
        }

        player.openMenu(new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int id, @NonNull Inventory inventory, @NonNull Player player) {
                return new WandMenu(id, inventory);
            }

            @Override
            public @NonNull Component getDisplayName() {
                return mainHand.getItemName();
            }
        });
    }

    @Override
    public @NonNull Type<WandGuiOpenPayload> type() {
        return TYPE;
    }
}
