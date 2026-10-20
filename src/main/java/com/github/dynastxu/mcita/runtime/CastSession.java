package com.github.dynastxu.mcita.runtime;

import com.github.dynastxu.mcita.core.action.Action;
import com.github.dynastxu.mcita.core.action.ActionOp;
import com.github.dynastxu.mcita.core.action.ShotConfig;
import com.github.dynastxu.mcita.core.action.ShotStat;
import com.github.dynastxu.mcita.core.action.StatOp;
import com.github.dynastxu.mcita.core.projectile.ProjectileProfile;
import com.github.dynastxu.mcita.core.projectile.TriggerSpec;
import com.github.dynastxu.mcita.core.wand.ActionCard;
import com.github.dynastxu.mcita.core.wand.WandStats;
import com.github.dynastxu.mcita.entity.SpellProjectile;
import com.github.dynastxu.mcita.registry.ModEntities;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The cast state machine, a direct port of {@code gun.lua}. One instance handles one
 * shot; call {@link #cast(int)} once and persist the returned {@link Result}.
 *
 * <p>Kept deliberately close to the Lua: always-cast cards are played first without
 * touching the deck, then {@code actions_per_round} cards are drawn, modifiers may
 * call {@link #drawActions}, and the hand moves to the discard pile at the end.
 */
public final class CastSession implements CastContext {
    private final ServerLevel level;
    private final LivingEntity caster;
    private final WandStats stats;
    private final Map<Integer, ActionCard> cards;
    private final ArrayDeque<Integer> drawPile;
    private final List<Integer> discardPile;
    private final List<Integer> hand = new ArrayList<>();
    private final RandomSource random;

    private ShotConfig shot;
    private float mana;
    private float reloadTicks;
    private float recoil;
    private boolean reloadStarted;

    public CastSession(ServerLevel level, LivingEntity caster, WandStats stats,
                       Map<Integer, ActionCard> cards, List<Integer> drawPile,
                       List<Integer> discardPile, float mana) {
        this.level = level;
        this.caster = caster;
        this.stats = stats;
        this.cards = new HashMap<>(cards);
        this.drawPile = new ArrayDeque<>(drawPile);
        this.discardPile = new ArrayList<>(discardPile);
        this.mana = mana;
        this.reloadTicks = stats.reloadTicks();
        this.random = level.getRandom();
    }

    public Result cast(int actionsPerRound) {
        this.shot = ShotConfig.of(stats);

        playAlwaysCast();
        drawActions(actionsPerRound, false);

        endShot();

        float castDelay = Math.max(0f, shot.get(ShotStat.FIRE_RATE_WAIT));
        if (recoil > 0f) {
            Vec3 look = caster.getLookAngle().scale(-recoil * 0.01);
            caster.push(look.x, look.y, look.z);
        }
        return new Result(Map.copyOf(cards), List.copyOf(drawPile), List.copyOf(discardPile),
                mana, castDelay, reloadTicks, reloadStarted);
    }

    private void playAlwaysCast() {
        for (Holder<Action> holder : stats.alwaysCast()) {
            for (ActionOp op : holder.value().ops()) {
                op.apply(this);
            }
        }
    }

    private void endShot() {
        for (int slot : hand) {
            ActionCard card = cards.get(slot);
            if (card == null) {
                continue;
            }
            if (card.usesRemaining() != 0) {
                discardPile.add(slot);
            }
        }
        hand.clear();

        reloadStarted = drawPile.isEmpty();
        if (drawPile.isEmpty()) {
            drawPile.addAll(discardPile);
            discardPile.clear();
            if (stats.shuffleWhenEmpty()) {
                List<Integer> shuffled = new ArrayList<>(drawPile);
                Collections.shuffle(shuffled, new java.util.Random(random.nextLong()));
                drawPile.clear();
                drawPile.addAll(shuffled);
            }
        }
    }

    @Override
    public void drawActions(int count, boolean instantReload) {
        for (int i = 0; i < count; i++) {
            if (!drawAction(instantReload)) {
                return;
            }
        }
    }

    private boolean drawAction(boolean instantReload) {
        if (drawPile.isEmpty()) {
            if (!instantReload) {
                return false;
            }
            if (discardPile.isEmpty()) {
                return false;
            }
            drawPile.addAll(discardPile);
            discardPile.clear();
            if (stats.shuffleWhenEmpty()) {
                List<Integer> shuffled = new ArrayList<>(drawPile);
                Collections.shuffle(shuffled, new java.util.Random(random.nextLong()));
                drawPile.clear();
                drawPile.addAll(shuffled);
            }
        }

        Integer slot = drawPile.pollFirst();
        if (slot == null) {
            return false;
        }
        ActionCard card = cards.get(slot);
        if (card == null) {
            return true;
        }
        if (card.isDepleted()) {
            discardPile.add(slot);
            return true;
        }

        Action action = card.action().value();
        float cost = action.manaDrain();
        if (cost > mana) {
            // Not enough mana: the card is skipped for this shot, as in gun.lua.
            discardPile.add(slot);
            return true;
        }
        mana = Math.max(0f, mana - cost);

        if (card.usesRemaining() > 0) {
            cards.put(slot, new ActionCard(card.action(), card.usesRemaining() - 1));
        }
        hand.add(slot);

        for (ActionOp op : action.ops()) {
            op.apply(this);
        }
        return true;
    }

    @Override
    public ShotConfig shot() {
        return shot;
    }

    @Override
    public void spawnProjectile(Holder<ProjectileProfile> projectile, int count, Optional<TriggerSpec> trigger) {
        ProjectileProfile profile = projectile.value();
        Vec3 direction = caster.getLookAngle();
        float speedMultiplier = Math.max(0f, shot.get(ShotStat.SPEED_MULTIPLIER));
        float spreadRadians = (float) Math.toRadians(Math.abs(shot.get(ShotStat.SPREAD_DEGREES)));

        for (int i = 0; i < count; i++) {
            SpellProjectile entity = ModEntities.SPELL_PROJECTILE.get().create(level, EntitySpawnReason.TRIGGERED);
            if (entity == null) {
                return;
            }
            entity.setPos(caster.getX(), caster.getEyeY() - 0.1, caster.getZ());
            entity.setOwner(caster);
            entity.configure(projectile, shot.copy());

            float speed = Mth.lerp(random.nextFloat(),
                    profile.motion().speedMin(), profile.motion().speedMax()) * speedMultiplier;
            entity.shoot(direction.x, direction.y, direction.z, Math.max(speed, 0f), spreadRadians);
            level.addFreshEntity(entity);
        }
    }

    @Override
    public void addReload(StatOp op) {
        reloadTicks = Math.max(0f, op.apply(reloadTicks));
    }

    @Override
    public void addRecoil(float amount) {
        recoil += amount;
    }

    @Override
    public void custom(Identifier behavior, CompoundTag params) {
        // TODO: registry of scripted card behaviours (ADD_TRIGGER, TAU, wand refresh).
    }

    public record Result(
            Map<Integer, ActionCard> cards,
            List<Integer> drawPile,
            List<Integer> discardPile,
            float mana,
            float castDelayTicks,
            float reloadTicks,
            boolean reloadStarted
    ) {
    }
}
