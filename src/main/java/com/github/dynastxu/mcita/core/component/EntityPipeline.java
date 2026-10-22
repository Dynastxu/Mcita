package com.github.dynastxu.mcita.core.component;

import java.util.List;

/**
 * Runs component systems in a fixed order until the entity dies.
 *
 * <p>One pipeline per entity kind, e.g. {@code ProjectileSystems.PIPELINE}. Every
 * system skips itself when the definition lacks its component, so instances of the
 * same entity class can behave differently purely because of their data.
 */
public final class EntityPipeline {
    private final List<ComponentSystem<?>> systems;

    public EntityPipeline(List<ComponentSystem<?>> systems) {
        this.systems = List.copyOf(systems);
    }

    public void tick(EntityContext context, EntityDefinition definition) {
        for (ComponentSystem<?> system : this.systems) {
            if (!context.alive()) {
                return;
            }
            system.tickAny(context, definition);
        }
    }
}
