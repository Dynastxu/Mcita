package com.github.dynastxu.mcita.item;

import com.github.dynastxu.mcita.core.action.Action;
import com.github.dynastxu.mcita.core.wand.ActionCard;
import com.github.dynastxu.mcita.core.wand.WandData;
import com.github.dynastxu.mcita.core.wand.WandStats;
import com.github.dynastxu.mcita.core.wand.WandTemplate;
import com.github.dynastxu.mcita.registry.ModDataComponents;
import com.github.dynastxu.mcita.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The only place that turns a preset template (or, later, rolled stats) into an
 * item stack. Keeping creation here means the item class never has to guess a
 * default and no wand can be silently initialized to another wand's data.
 */
public final class WandFactory {
    private WandFactory() {
    }

    public static ItemStack preset(Holder<WandTemplate> template) {
        return preset(template.value());
    }

    public static ItemStack preset(WandTemplate template) {
        return create(template.stats(), template.deck());
    }

    public static ItemStack create(WandStats stats, List<Holder<Action>> deck) {
        ItemStack stack = new ItemStack(ModItems.WAND.get());

        Map<Integer, ActionCard> cards = new HashMap<>();
        List<Integer> draw = new ArrayList<>();
        int capacity = Math.max(0, stats.deckCapacity());
        for (int slot = 0; slot < Math.min(deck.size(), capacity); slot++) {
            cards.put(slot, new ActionCard(deck.get(slot), Action.INFINITE_USES));
            draw.add(slot);
        }

        stack.set(ModDataComponents.WAND.get(), new WandData(
                stats, cards, draw, List.of(),
                stats.manaMax(), 0L, 0L, 0L, false));
        stack.set(DataComponents.ITEM_MODEL, itemModel(stats.cosmetic().model()));
        return stack;
    }

    /**
     * Cosmetic ids were originally texture paths ({@code mcita:item/wand_x}); an
     * item-model id drops the {@code item/} segment. Accept both so existing
     * datapack entries keep working.
     */
    private static Identifier itemModel(Identifier cosmeticModel) {
        String path = cosmeticModel.getPath();
        if (path.startsWith("item/")) {
            path = path.substring("item/".length());
        }
        return Identifier.fromNamespaceAndPath(cosmeticModel.getNamespace(), path);
    }
}
