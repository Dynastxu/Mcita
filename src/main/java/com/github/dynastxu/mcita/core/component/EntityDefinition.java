package com.github.dynastxu.mcita.core.component;

import java.util.List;
import java.util.Optional;

/**
 * A named bag of components, the port of one Noita entity XML file.
 *
 * <p>Implementations must be immutable and shared: every entity spawned from the
 * same registry entry points at the same definition, and per-instance state lives
 * on the entity. {@code ProjectileProfile} is the first implementation; a future
 * entity definition will simply hold a data-loaded component list.
 */
public interface EntityDefinition {
    List<EntityComponent> components();

    /** First component of the requested type, or empty if the entity lacks it. */
    default <C extends EntityComponent> Optional<C> find(Class<C> type) {
        for (EntityComponent component : components()) {
            if (type.isInstance(component)) {
                return Optional.of(type.cast(component));
            }
        }
        return Optional.empty();
    }
}
