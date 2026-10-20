package com.github.dynastxu.mcita.registry;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.menu.WandMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, Mcita.MOD_ID);

    /**
     * One menu type for every wand: the slot count is derived from the wand's own
     * {@code WandData}, so there is no need for NoitaCore's 66 menu variants.
     */
    public static final DeferredHolder<MenuType<?>, MenuType<WandMenu>> WAND =
            MENU_TYPES.register("wand", () -> new MenuType<>(WandMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenuTypes() {
    }
}
