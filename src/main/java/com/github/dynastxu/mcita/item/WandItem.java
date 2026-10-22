package com.github.dynastxu.mcita.item;

import com.github.dynastxu.mcita.core.wand.WandData;
import com.github.dynastxu.mcita.core.wand.WandStats;
import com.github.dynastxu.mcita.registry.ModDataComponents;
import com.github.dynastxu.mcita.runtime.CastSession;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/**
 * Carrier item for every wand. The concrete wand is the {@code mcita:wand}
 * data component; this class only casts and opens the GUI.
 *
 * <p>A stack without that component is an uninitialized debug item. It is never
 * produced by survival acquisition and does nothing when used, so the bare
 * {@code mcita:wand} id is effectively creative-only.
 */
public class WandItem extends Item {
    public WandItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull Component getName(ItemStack stack) {
        WandData data = stack.get(ModDataComponents.WAND.get());
        if (data == null) {
            return Component.translatable("item.mcita.wand.uninitialized");
        }

        String key = data.stats().cosmetic().nameKey();
        if (!key.isEmpty()) {
            return Component.translatable(key);
        }
        return super.getName(stack);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        WandData data = stack.get(ModDataComponents.WAND.get());
        if (data == null) {
            return InteractionResult.PASS;
        }

        return cast(server, player, stack, data, hand);
    }

    private InteractionResult cast(ServerLevel server, Player player, ItemStack stack, WandData data,
                                   InteractionHand hand) {
        WandStats stats = data.stats();
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
}
