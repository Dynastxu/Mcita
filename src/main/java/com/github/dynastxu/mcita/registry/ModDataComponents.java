package com.github.dynastxu.mcita.registry;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.core.wand.ActionCard;
import com.github.dynastxu.mcita.core.wand.WandData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Mcita.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ActionCard>> ACTION_CARD =
            COMPONENTS.registerComponentType("action_card", builder -> builder
                    .persistent(ActionCard.CODEC)
                    .networkSynchronized(ActionCard.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WandData>> WAND =
            COMPONENTS.registerComponentType("wand", builder -> builder
                    .persistent(WandData.CODEC));

    private ModDataComponents() {
    }
}
