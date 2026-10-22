package com.github.dynastxu.mcita.core.component.projectile;

import com.github.dynastxu.mcita.core.component.EntityContext;
import com.github.dynastxu.mcita.core.component.EntityDefinition;
import com.github.dynastxu.mcita.core.projectile.ProjectileProfile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * What happens when a projectile dies, the port of
 * {@code ProjectileComponent.on_death_explode} / {@code on_lifetime_out_explode}.
 * Shared by the lifetime and collision systems so the order of operations stays in
 * one place.
 */
final class Detonation {
    private Detonation() {
    }

    static void detonate(EntityContext context, EntityDefinition definition) {
        if (context.level() instanceof ServerLevel server) {
            Optional<ProjectileProfile.Explosion> explosion =
                    definition.find(ProjectileProfile.Explosion.class);
            if (explosion.isPresent() && explosion.get().radius() > 0f) {
                context.explode(explosion.get().radius());
            } else {
                server.sendParticles(ParticleTypes.CRIT, context.position().x, context.position().y,
                        context.position().z, 4, 0.1, 0.1, 0.1, 0.01);
            }
        }
        context.despawn();
    }
}
