package com.github.dynastxu.mcita.network;

import com.github.dynastxu.mcita.Mcita;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jspecify.annotations.NonNull;

@EventBusSubscriber(modid = Mcita.MOD_ID)
public final class ModPayloads {
    private ModPayloads() {
    }

    @SubscribeEvent
    public static void register(@NonNull RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                WandGuiOpenPayload.TYPE,
                WandGuiOpenPayload.STREAM_CODEC,
                WandGuiOpenPayload::handle);
    }
}
