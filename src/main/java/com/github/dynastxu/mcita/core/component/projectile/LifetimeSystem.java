package com.github.dynastxu.mcita.core.component.projectile;

import com.github.dynastxu.mcita.core.component.ComponentSystem;
import com.github.dynastxu.mcita.core.component.EntityContext;
import com.github.dynastxu.mcita.core.component.EntityDefinition;
import com.github.dynastxu.mcita.core.projectile.ProjectileProfile;

/**
 * Counts down {@code lifetime} and detonates on expiry, the port of
 * {@code ProjectileComponent.lifetime} together with
 * {@code on_lifetime_out_explode}.
 *
 * <p>Runs before collision and motion so an expired projectile never moves on its
 * last tick, matching the original Noita tick order.
 */
public final class LifetimeSystem implements ComponentSystem<ProjectileProfile.Lifetime> {
    @Override
    public Class<ProjectileProfile.Lifetime> componentType() {
        return ProjectileProfile.Lifetime.class;
    }

    @Override
    public void tick(EntityContext context, EntityDefinition definition, ProjectileProfile.Lifetime lifetime) {
        // Post-decrement, like Noita's `if (remaining_life-- <= 0)`: a lifetime of 1
        // still gets one full tick of movement before it dies.
        float current = context.remainingLife();
        context.setRemainingLife(current - 1f);
        if (current <= 0f) {
            Detonation.detonate(context, definition);
        }
    }
}
