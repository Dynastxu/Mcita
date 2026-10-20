package com.github.dynastxu.mcita.registry;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.item.ActionCardItem;
import com.github.dynastxu.mcita.item.WandItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Mcita.MOD_ID);

    public static final DeferredItem<ActionCardItem> ACTION_CARD =
            ITEMS.registerItem("action_card", ActionCardItem::new, properties -> properties.stacksTo(1));

    public static final DeferredItem<WandItem> WAND =
            ITEMS.registerItem("wand", WandItem::new, properties -> properties.stacksTo(1));

    private ModItems() {
    }
}
