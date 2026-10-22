package com.github.dynastxu.mcita.core.component.projectile;

import com.github.dynastxu.mcita.core.component.ComponentSystem;
import com.github.dynastxu.mcita.core.component.EntityContext;
import com.github.dynastxu.mcita.core.component.EntityDefinition;
import com.github.dynastxu.mcita.core.projectile.ProjectileProfile;
import net.minecraft.world.phys.Vec3;

/**
 * Integrates position and applies Noita's drag, fluid drag and gravity, the port of
 * {@code VelocityComponent} plus the motion fields of {@code ProjectileComponent}.
 *
 * <p>Runs last so collision has already resolved this tick's hit.
 */
public final class MotionSystem implements ComponentSystem<ProjectileProfile.Motion> {
    @Override
    public Class<ProjectileProfile.Motion> componentType() {
        return ProjectileProfile.Motion.class;
    }

    @Override
    public void tick(EntityContext context, EntityDefinition definition, ProjectileProfile.Motion motion) {
        if (context.stuck()) {
            return;
        }

        Vec3 delta = context.velocity();
        context.setPosition(context.position().add(delta));

        Vec3 velocity = applyDrag(context, delta, motion.airFriction());
        if (motion.gravity() != 0f) {
            velocity = new Vec3(velocity.x, velocity.y - motion.gravity(), velocity.z);
        }
        context.setVelocity(velocity);
    }

    /**
     * {@code air_friction} is a per-frame coefficient (0 = vacuum, larger = more
     * drag, negative = thrust, as used by Accelerating Shot), so 3 frames per tick
     * means cubing the per-frame factor. Inside a liquid Noita switches to quadratic
     * drag instead, scaled by the same coefficient.
     */
    private static Vec3 applyDrag(EntityContext context, Vec3 velocity, float airFriction) {
        if (context.inLiquid()) {
            float drag = airFriction / 10.5f;
            return new Vec3(
                    velocity.x - drag * velocity.x * Math.abs(velocity.x),
                    velocity.y - drag * velocity.y * Math.abs(velocity.y),
                    velocity.z - drag * velocity.z * Math.abs(velocity.z));
        }
        if (airFriction == 0f) {
            return velocity;
        }
        float perFrame = Math.max(0f, 1f - airFriction / 60f);
        return velocity.scale(perFrame * perFrame * perFrame);
    }
}
