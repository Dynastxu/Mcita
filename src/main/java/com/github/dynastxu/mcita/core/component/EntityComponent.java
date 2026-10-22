package com.github.dynastxu.mcita.core.component;

import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * One piece of data describing what an entity does, the port of a Noita entity
 * XML component such as {@code ProjectileComponent} or {@code VelocityComponent}.
 *
 * <p>A component carries parameters only; the behaviour lives in a
 * {@link ComponentSystem}. An {@link EntityDefinition} lists the components it
 * has, and every system whose component type is present runs against it. Adding
 * a new behaviour therefore means adding a component and a system, never editing
 * the entity class.
 */
public interface EntityComponent {
    /**
     * Stable discriminator used by the data format, e.g. {@code "motion"}.
     * Matches the lower snake case name of the original Noita component.
     */
    @NonNull String type();

    /**
     * Noita's {@code _tags}, used to switch a component on only in a given
     * context ({@code enabled_in_world}, {@code mortal}, ...). Empty means always
     * active.
     */
    @NonNull default List<String> tags() {
        return List.of();
    }
}
