package com.github.dynastxu.mcita;

import com.github.dynastxu.mcita.command.McitaCommands;
import com.github.dynastxu.mcita.datagen.McitaDataGen;
import com.github.dynastxu.mcita.registry.ModCreativeTabs;
import com.github.dynastxu.mcita.registry.ModDataComponents;
import com.github.dynastxu.mcita.registry.ModEntities;
import com.github.dynastxu.mcita.registry.ModItems;
import com.github.dynastxu.mcita.registry.ModMenuTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

@Mod(Mcita.MOD_ID)
public final class Mcita {
    public static final String MOD_ID = "mcita";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Mcita(IEventBus modBus, ModContainer container) {
        ModDataComponents.COMPONENTS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModMenuTypes.MENU_TYPES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        modBus.addListener(McitaRegistries::onNewRegistry);
        modBus.addListener(McitaDataGen::gatherData);

        NeoForge.EVENT_BUS.addListener(Mcita::onServerStarted);
        NeoForge.EVENT_BUS.addListener(McitaCommands::register);

        LOGGER.info("Mcita loaded");
    }

    private static void onServerStarted(ServerStartedEvent event) {
        var registries = event.getServer().registryAccess();
        LOGGER.info("Mcita data: {} actions, {} projectiles, {} wands",
                registries.lookupOrThrow(McitaRegistries.ACTION).size(),
                registries.lookupOrThrow(McitaRegistries.PROJECTILE).size(),
                registries.lookupOrThrow(McitaRegistries.WAND).size());
    }

    /** Every mcita identifier goes through here. */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
