package com.github.dynastxu.mcita.menu;

import com.github.dynastxu.mcita.core.wand.ActionCard;
import com.github.dynastxu.mcita.core.wand.WandData;
import com.github.dynastxu.mcita.item.ActionCardItem;
import com.github.dynastxu.mcita.registry.ModDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Slot view over {@link WandData#cards()}. Ported from NoitaCore's WandContainer, but the
 * backing store is the new card map instead of an ItemStack list on the component.
 */
public class WandContainer implements Container {
    private final int capacity;
    private final NonNullList<ItemStack> items;
    private final ItemStack wand;

    public WandContainer(@Nullable WandData data, ItemStack wand) {
        this.capacity = data == null ? 0 : Math.max(0, data.stats().deckCapacity());
        this.items = NonNullList.withSize(this.capacity, ItemStack.EMPTY);
        if (data != null) {
            for (Map.Entry<Integer, ActionCard> entry : data.cards().entrySet()) {
                int slot = entry.getKey();
                if (slot >= 0 && slot < this.capacity) {
                    this.items.set(slot, ActionCardItem.stackFor(entry.getValue()));
                }
            }
        }
        this.wand = wand;
    }

    @Override
    public int getContainerSize() {
        return capacity;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        return slot >= 0 && slot < capacity ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int count) {
        return removeItem(slot);
    }

    public @NonNull ItemStack removeItem(int slot) {
        if (slot < 0 || slot >= capacity) {
            return ItemStack.EMPTY;
        }
        ItemStack result = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        return removeItem(slot);
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack stack) {
        if (slot >= 0 && slot < capacity) {
            items.set(slot, stack);
            setChanged();
        }
    }

    @Override
    public void setChanged() {
        WandData current = wand.get(ModDataComponents.WAND.get());
        if (current == null) {
            return;
        }

        Map<Integer, ActionCard> cards = new HashMap<>();
        List<Integer> draw = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            ActionCard card = items.get(i).get(ModDataComponents.ACTION_CARD.get());
            if (card != null) {
                cards.put(i, card);
                draw.add(i);
            }
        }

        // Editing the deck rebuilds the draw pile; mana and cooldowns are preserved.
        wand.set(ModDataComponents.WAND.get(), current.withState(
                cards, draw, List.of(), current.mana(), current.lastManaTick(),
                current.nextCastTick(), current.nextReloadTick(), false));
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        Collections.fill(items, ItemStack.EMPTY);
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(@NonNull ItemStack stack) {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
        return stack.getItem() instanceof ActionCardItem
                && stack.has(ModDataComponents.ACTION_CARD.get());
    }

    @Override
    public boolean canTakeItem(@NonNull Container into, int slot, @NonNull ItemStack stack) {
        return false;
    }
}
