package com.github.dynastxu.mcita.core.component.projectile;

import com.github.dynastxu.mcita.core.action.ShotStat;
import com.github.dynastxu.mcita.core.component.ComponentSystem;
import com.github.dynastxu.mcita.core.component.EntityContext;
import com.github.dynastxu.mcita.core.component.EntityDefinition;
import com.github.dynastxu.mcita.core.projectile.ProjectileProfile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Raycasts this tick's movement and resolves the hit: damage, bounce, sticking or
 * detonation. This is Noita's {@code ProjectileComponent} collision handling, which
 * reads the motion fields of the same component and the entity's
 * {@code DamageModelComponent}.
 *
 * <p>Runs before {@link MotionSystem}: a projectile that hits this tick must not
 * also move through the surface.
 */
public final class CollisionSystem implements ComponentSystem<ProjectileProfile.Motion> {
    @Override
    public Class<ProjectileProfile.Motion> componentType() {
        return ProjectileProfile.Motion.class;
    }

    @Override
    public void tick(EntityContext context, EntityDefinition definition, ProjectileProfile.Motion motion) {
        if (context.stuck()) {
            return;
        }

        HitResult hit = context.raycastMove();
        if (hit.getType() == HitResult.Type.MISS) {
            return;
        }

        // Resolve where the projectile actually hit, not where it started this tick.
        context.setPosition(hit.getLocation());

        if (hit instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity target) {
            dealDamage(context, definition, target);
        }

        if (hit instanceof BlockHitResult blockHit) {
            if (bounce(context, motion, blockHit)) {
                return;
            }
            if (!motion.dieOnCollision()) {
                // on_collision_die = 0: stop against the surface, lifetime still runs out.
                context.setStuck(true);
                context.setVelocity(Vec3.ZERO);
                return;
            }
        }

        Detonation.detonate(context, definition);
    }

    private static void dealDamage(EntityContext context, EntityDefinition definition, LivingEntity target) {
        definition.find(ProjectileProfile.Damage.class).ifPresent(damage -> {
            float bonus = context.shotValue(ShotStat.DAMAGE_PROJECTILE_ADD);
            float amount = Math.max(0f, damage.amount() + bonus);
            if (amount > 0f) {
                context.hurtTarget(target, amount, damage.kind());
            }
        });
    }

    private static boolean bounce(EntityContext context, ProjectileProfile.Motion motion, BlockHitResult hit) {
        if (context.remainingBounces() <= 0) {
            return false;
        }
        context.setRemainingBounces(context.remainingBounces() - 1);

        Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getUnitVec3i());
        Vec3 velocity = context.velocity();
        double dot = velocity.dot(normal);
        if (dot < 0) {
            velocity = velocity.subtract(normal.scale(2 * dot));
        }
        context.setVelocity(velocity.scale(Math.max(0f, motion.bounceEnergy())));

        // Step off the face so next tick's ray does not start inside the block.
        Vec3 offset = context.position().add(normal.x * 0.05, normal.y * 0.05, normal.z * 0.05);
        context.setPosition(offset);
        return true;
    }
}
