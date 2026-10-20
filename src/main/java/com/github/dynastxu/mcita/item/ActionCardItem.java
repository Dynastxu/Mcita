package com.github.dynastxu.mcita.item;

import com.github.dynastxu.mcita.core.wand.ActionCard;
import com.github.dynastxu.mcita.registry.ModDataComponents;
import com.github.dynastxu.mcita.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

/** A physical spell card; its action lives in the {@code mcita:action_card} component. */
public class ActionCardItem extends Item {
    public ActionCardItem(Properties properties) {
        super(properties);
    }

    public static ItemStack stackFor(ActionCard card) {
        ItemStack stack = new ItemStack(ModItems.ACTION_CARD.get());
        stack.set(ModDataComponents.ACTION_CARD.get(), card);
        card.action().value().cardModel()
                .ifPresent(model -> stack.set(DataComponents.ITEM_MODEL, model));
        return stack;
    }

    @Override
    public @NonNull Component getName(ItemStack stack) {
        ActionCard card = stack.get(ModDataComponents.ACTION_CARD.get());
        if (card == null) {
            return super.getName(stack);
        }
        String path = card.action().unwrapKey()
                .map(ResourceKey::identifier)
                .map(Identifier::getPath)
                .orElse("unknown");
        return Component.translatable("item.mcita.spell_" + path);
    }
}
