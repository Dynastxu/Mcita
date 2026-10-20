package com.github.dynastxu.mcita.registry;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.McitaRegistries;
import com.github.dynastxu.mcita.core.action.Action;
import com.github.dynastxu.mcita.core.wand.ActionCard;
import com.github.dynastxu.mcita.item.ActionCardItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Mcita.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mcita"))
                    .icon(() -> new ItemStack(ModItems.ACTION_CARD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.WAND.get());
                        parameters.holders()
                                .lookupOrThrow(McitaRegistries.ACTION)
                                .listElements()
                                .map(holder -> (Holder<Action>) holder)
                                .map(holder -> ActionCardItem.stackFor(
                                        new ActionCard(holder, Action.INFINITE_USES)))
                                .forEach(output::accept);
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
