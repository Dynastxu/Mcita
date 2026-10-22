package com.github.dynastxu.mcita.core.component;

import com.github.dynastxu.mcita.core.action.ShotStat;
import com.github.dynastxu.mcita.core.projectile.DamageKind;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The simulation view a {@link ComponentSystem} is allowed to touch.
 *
 * <p>This is the seam that keeps systems independent of a concrete entity class
 * (dependency inversion): {@code SpellProjectile} implements it today, and a
 * future custom mob can reuse the same movement and collision systems through its
 * own adapter.
 */
public interface EntityContext {
    Level level();

    /** Explosion source and raycast anchor. */
    Entity entity();

    Vec3 position();

    void setPosition(Vec3 position);

    Vec3 velocity();

    void setVelocity(Vec3 velocity);

    boolean inLiquid();

    /** Noita's {@code lifetime}: ticks left before the projectile detonates. */
    float remainingLife();

    void setRemainingLife(float ticks);

    int remainingBounces();

    void setRemainingBounces(int bounces);

    boolean stuck();

    void setStuck(boolean stuck);

    /** False once the entity has been discarded, which stops the pipeline. */
    boolean alive();

    void despawn();

    /** Raycast along this tick's movement, filtered by {@link #canHit}. */
    HitResult raycastMove();

    boolean canHit(Entity target);

    void hurtTarget(LivingEntity target, float amount, DamageKind kind);

    void explode(float radius);

    /**
     * Read-only view of Noita's per-shot {@code c} table, used by modifiers that
     * add to the projectile's own numbers.
     */
    float shotValue(ShotStat stat);
}
