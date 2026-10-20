package com.github.dynastxu.mcita.menu;

import com.github.dynastxu.mcita.core.wand.WandData;
import com.github.dynastxu.mcita.item.ActionCardItem;
import com.github.dynastxu.mcita.registry.ModDataComponents;
import com.github.dynastxu.mcita.registry.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

/** Ported from NoitaCore's WandMenu; reads the wand from the main hand instead of a payload. */
public class WandMenu extends AbstractContainerMenu {
    private static final int SLOTS_PER_ROW = 11;
    private static final int ROWS_PER_PAGE = 3;
    private static final int SLOTS_PER_PAGE = SLOTS_PER_ROW * ROWS_PER_PAGE;

    private final WandContainer wandContainer;
    private final Inventory playerInventory;
    private final ItemStack wand;
    private final DataSlot currentPage;
    private final int totalPages;

    public WandMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, playerInventory.player.getMainHandItem());
    }

    private WandMenu(int containerId, Inventory playerInventory, ItemStack wand) {
        super(ModMenuTypes.WAND.get(), containerId);
        WandData data = wand.get(ModDataComponents.WAND.get());

        this.wandContainer = new WandContainer(data, wand);
        this.playerInventory = playerInventory;
        this.wand = wand;
        this.currentPage = DataSlot.standalone();

        int size = wandContainer.getContainerSize();
        this.totalPages = Math.max(1, (int) Math.ceil((double) size / SLOTS_PER_PAGE));
        this.addDataSlot(currentPage);

        addWandSlots();
        addStandardInventorySlots(playerInventory, 35, 84);
    }

    public int getCurrentPage() {
        return currentPage.get();
    }

    public int getTotalPages() {
        return totalPages;
    }

    private void addWandSlots() {
        int left = 17;
        int top = 18;

        for (int i = 0; i < wandContainer.getContainerSize(); i++) {
            int page = i / SLOTS_PER_PAGE;
            int posInPage = i % SLOTS_PER_PAGE;
            int row = posInPage / SLOTS_PER_ROW;
            int col = posInPage % SLOTS_PER_ROW;

            this.addSlot(new Slot(wandContainer, i, left + col * 18, top + row * 18) {
                @Override
                public boolean isActive() {
                    return page == currentPage.get();
                }
            });
        }
    }

    @Override
    public boolean clickMenuButton(@NonNull Player player, int buttonId) {
        if (buttonId == 0 && currentPage.get() > 0) {
            currentPage.set(currentPage.get() - 1);
            return true;
        }
        if (buttonId == 1 && currentPage.get() < totalPages - 1) {
            currentPage.set(currentPage.get() + 1);
            return true;
        }
        return false;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        Slot slot = getSlot(slotIndex);
        ItemStack item = slot.getItem();
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack before = item.copy();
        if (slot.container instanceof Inventory) {
            int startSlot = SLOTS_PER_PAGE * currentPage.get();
            int slotsOnPage = currentPage.get() == totalPages - 1
                    ? wandContainer.getContainerSize() % SLOTS_PER_PAGE
                    : SLOTS_PER_PAGE;
            if (slotsOnPage == 0) {
                slotsOnPage = SLOTS_PER_PAGE;
            }
            moveItemStackTo(item, startSlot, Math.min(startSlot + slotsOnPage, slots.size()), false);
        } else if (slot.container instanceof WandContainer) {
            moveItemStackTo(item, wandContainer.getContainerSize(), slots.size(), false);
        }

        return ItemStack.matches(item, before) ? ItemStack.EMPTY : before;
    }

    @Override
    protected boolean moveItemStackTo(@NonNull ItemStack stack, int startSlot, int endSlot, boolean backwards) {
        if (!stack.isEmpty()
                && slots.get(startSlot).container instanceof WandContainer
                && !(stack.getItem() instanceof ActionCardItem)) {
            return false;
        }
        return super.moveItemStackTo(stack, startSlot, endSlot, backwards);
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return player.getMainHandItem() == wand;
    }
}
