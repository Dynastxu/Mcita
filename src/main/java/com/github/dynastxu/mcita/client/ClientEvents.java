package com.github.dynastxu.mcita.client;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.client.gui.WandTooltipComponent;
import com.github.dynastxu.mcita.core.wand.WandData;
import com.github.dynastxu.mcita.item.WandItem;
import com.github.dynastxu.mcita.network.WandGuiOpenPayload;
import com.github.dynastxu.mcita.registry.ModDataComponents;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;

@EventBusSubscriber(modid = Mcita.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (KeyMappings.OPEN_WAND_GUI.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new WandGuiOpenPayload());
        }
    }

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.@NonNull GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof WandItem)) {
            return;
        }
        WandData data = stack.get(ModDataComponents.WAND.get());
        if (data != null) {
            event.getTooltipElements().add(Either.right(new WandTooltipComponent(data)));
        }
    }
}
