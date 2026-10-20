package com.github.dynastxu.mcita.client;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.client.gui.WandClientTooltipComponent;
import com.github.dynastxu.mcita.client.gui.WandTooltipComponent;
import com.github.dynastxu.mcita.client.screen.WandScreen;
import com.github.dynastxu.mcita.menu.WandMenu;
import com.github.dynastxu.mcita.registry.ModEntities;
import com.github.dynastxu.mcita.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Mcita.MOD_ID, value = Dist.CLIENT)
public final class McitaClient {
    private McitaClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SPELL_PROJECTILE.get(), SpellProjectileRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        // 这里必须显式指定类型，否则会报错
        // 但 IDEA 警告为显式类型实参可以推断，原因不明
        //noinspection RedundantTypeArguments
        event.<WandMenu, WandScreen>register(ModMenuTypes.WAND.get(), WandScreen::new);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyMappings.OPEN_WAND_GUI.get());
    }

    @SubscribeEvent
    public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(WandTooltipComponent.class, WandClientTooltipComponent::new);
    }
}
