package com.github.dynastxu.mcita.core.component;

/**
 * Behaviour for one component type, the port of the engine-side update of a Noita
 * component ({@code VelocityComponent}, {@code ProjectileComponent}, ...).
 *
 * <p>Systems are stateless and are run in a fixed order by an
 * {@link EntityPipeline}. The order is code, never data: a data pack declares
 * which components exist, but it cannot change simulation semantics.
 */
public interface ComponentSystem<C extends EntityComponent> {
    Class<C> componentType();

    void tick(EntityContext context, EntityDefinition definition, C component);

    /**
     * Runs this system only if the definition carries its component, which is what
     * makes an entity's behaviour depend on the data rather than on its Java class.
     */
    default void tickAny(EntityContext context, EntityDefinition definition) {
        definition.find(componentType())
                .ifPresent(component -> tick(context, definition, component));
    }
}
