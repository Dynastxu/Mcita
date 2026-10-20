package com.github.dynastxu.mcita.item;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.McitaRegistries;
import com.github.dynastxu.mcita.core.action.Action;
import com.github.dynastxu.mcita.core.wand.ActionCard;
import com.github.dynastxu.mcita.core.wand.WandData;
import com.github.dynastxu.mcita.core.wand.WandStats;
import com.github.dynastxu.mcita.registry.ModDataComponents;
import com.github.dynastxu.mcita.runtime.CastSession;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal playable wand: right click runs one shot through {@link CastSession}.
 * The starter deck is temporary until cards exist as real items.
 */
public class WandItem extends Item {
    private static final Identifier DEFAULT_WAND = Mcita.id("starting_wand");

    /** Temporary starter deck so the first vertical slice is playable. */
    private static final List<Identifier> STARTER_DECK = List.of(
            Mcita.id("light_bullet"),
            Mcita.id("bomb"),
            Mcita.id("burst_2"),
            Mcita.id("critical_hit"));

    public WandItem(Properties properties) {
        super(properties);
    }

    /**
     * A wand can enter the inventory from creative, /give or crafting, so the deck has
     * to exist before the first cast: the GUI opens from the component and must not
     * depend on the player firing once.
     */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
        super.inventoryTick(stack, level, owner, slot);
        if (stack.has(ModDataComponents.WAND.get())) {
            return;
        }

        Holder<WandStats> stats = holder(level, McitaRegistries.WAND, DEFAULT_WAND);
        if (stats != null) {
            stack.set(ModDataComponents.WAND.get(), createInitialData(level, stats));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        WandData data = stack.get(ModDataComponents.WAND.get());
        if (data != null) {
            String key = data.stats().value().cosmetic().nameKey();
            if (!key.isEmpty()) {
                return Component.translatable(key);
            }
        }
        return super.getName(stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        WandData data = stack.get(ModDataComponents.WAND.get());
        if (data == null) {
            Holder<WandStats> stats = holder(server, McitaRegistries.WAND, DEFAULT_WAND);
            if (stats == null) {
                return InteractionResult.PASS;
            }
            data = createInitialData(server, stats);
        }

        return cast(server, player, stack, data, hand);
    }

    private InteractionResult cast(ServerLevel server, Player player, ItemStack stack, WandData data,
                                   InteractionHand hand) {
        WandStats stats = data.stats().value();
        long now = server.getGameTime();

        float mana = Math.min(stats.manaMax(),
                data.mana() + (now - data.lastManaTick()) * stats.manaChargeSpeed() / 20f);

        if (data.reloading() && now < data.nextReloadTick()) {
            return InteractionResult.FAIL;
        }
        if (now < data.nextCastTick()) {
            return InteractionResult.FAIL;
        }

        CastSession.Result result = new CastSession(server, player, stats,
                data.cards(), data.drawPile(), data.discardPile(), mana)
                .cast(Math.max(1, stats.actionsPerRound()));

        long nextCast = result.castDelayTicks() > 0f
                ? now + (long) Math.ceil(result.castDelayTicks())
                : 0L;
        long nextReload = result.reloadStarted()
                ? now + (long) Math.ceil(Math.max(result.reloadTicks(), 1f))
                : 0L;

        stack.set(ModDataComponents.WAND.get(), data.withState(
                result.cards(), result.drawPile(), result.discardPile(), result.mana(),
                now, nextCast, nextReload, result.reloadStarted()));

        player.swing(hand, true);
        return InteractionResult.SUCCESS;
    }

    private static WandData createInitialData(ServerLevel server, Holder<WandStats> statsHolder) {
        WandStats stats = statsHolder.value();
        Map<Integer, ActionCard> cards = new HashMap<>();
        List<Integer> draw = new ArrayList<>();

        int slot = 0;
        for (Identifier id : STARTER_DECK) {
            if (slot >= stats.deckCapacity()) {
                break;
            }
            Holder<Action> action = holder(server, McitaRegistries.ACTION, id);
            if (action != null) {
                cards.put(slot, new ActionCard(action, Action.INFINITE_USES));
                draw.add(slot);
                slot++;
            }
        }

        return new WandData(statsHolder, cards, draw, List.of(),
                stats.manaMax(), 0L, 0L, 0L, false);
    }

    private static <T> Holder<T> holder(ServerLevel level, ResourceKey<Registry<T>> registry, Identifier id) {
        return level.registryAccess()
                .lookupOrThrow(registry)
                .get(id)
                .<Holder<T>>map(holder -> holder)
                .orElse(null);
    }
}
