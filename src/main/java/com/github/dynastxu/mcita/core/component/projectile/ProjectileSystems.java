package com.github.dynastxu.mcita.core.component.projectile;

import com.github.dynastxu.mcita.core.component.EntityPipeline;

import java.util.List;

/**
 * The projectile tick order, the closest Minecraft equivalent of Noita running
 * every component's update pass in a fixed engine order.
 *
 * <p>Order matters: lifetime expires before collision, collision resolves the hit
 * before motion moves the projectile into the surface.
 */
public final class ProjectileSystems {
    public static final EntityPipeline PIPELINE = new EntityPipeline(List.of(
            new LifetimeSystem(),
            new CollisionSystem(),
            new MotionSystem()));

    private ProjectileSystems() {
    }
}
