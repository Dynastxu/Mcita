package com.github.dynastxu.mcita.core.projectile;

import com.github.dynastxu.mcita.core.component.EntityComponent;
import com.github.dynastxu.mcita.core.component.EntityDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data for one projectile, the port of a Noita projectile entity XML.
 * All numbers are in Minecraft units (blocks, ticks) after datagen conversion.
 *
 * <p>The record is a {@link EntityDefinition}: its parts are components and the
 * {@code core.component.projectile} systems run over whichever of them are
 * present. The JSON shape stays flat for now so existing data keeps loading; the
 * next step replaces the record with a data-loaded component list.
 */
public record ProjectileProfile(
        Motion motion,
        Lifetime lifetime,
        Damage damage,
        Optional<Explosion> explosion,
        Optional<TriggerSpec> trigger,
        Optional<Render> render
) implements EntityDefinition {
    public static final Codec<ProjectileProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Motion.CODEC.fieldOf("motion").forGetter(ProjectileProfile::motion),
            Lifetime.CODEC.fieldOf("lifetime").forGetter(ProjectileProfile::lifetime),
            Damage.CODEC.fieldOf("damage").forGetter(ProjectileProfile::damage),
            Explosion.CODEC.optionalFieldOf("explosion").forGetter(ProjectileProfile::explosion),
            TriggerSpec.CODEC.optionalFieldOf("trigger").forGetter(ProjectileProfile::trigger),
            Render.CODEC.optionalFieldOf("render").forGetter(ProjectileProfile::render)
    ).apply(instance, ProjectileProfile::new));

    @Override
    public List<EntityComponent> components() {
        List<EntityComponent> components = new ArrayList<>(6);
        components.add(motion);
        components.add(lifetime);
        components.add(damage);
        explosion.ifPresent(components::add);
        trigger.ifPresent(components::add);
        render.ifPresent(components::add);
        return List.copyOf(components);
    }

    /**
     * Noita's {@code VelocityComponent} plus the motion fields of {@code ProjectileComponent}.
     * Speeds are blocks per tick, gravity is blocks per tick squared, and
     * {@code airFriction} keeps Noita's per-frame drag coefficient (0 = no drag,
     * larger = more drag, negative = thrust). {@code bounces} is the initial bounce
     * count and {@code bounceEnergy} the velocity kept after each bounce.
     */
    public record Motion(
            float speedMin,
            float speedMax,
            float gravity,
            float airFriction,
            float mass,
            float friction,
            int bounces,
            float bounceEnergy,
            boolean dieOnCollision
    ) implements EntityComponent {
        @Override
        public @NonNull String type() {
            return "motion";
        }

        public static final Codec<Motion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("speed_min").forGetter(Motion::speedMin),
                Codec.FLOAT.fieldOf("speed_max").forGetter(Motion::speedMax),
                Codec.FLOAT.optionalFieldOf("gravity", 0f).forGetter(Motion::gravity),
                Codec.FLOAT.optionalFieldOf("air_friction", 1f).forGetter(Motion::airFriction),
                Codec.FLOAT.optionalFieldOf("mass", 0.05f).forGetter(Motion::mass),
                Codec.FLOAT.optionalFieldOf("friction", 1f).forGetter(Motion::friction),
                Codec.INT.optionalFieldOf("bounces", 0).forGetter(Motion::bounces),
                Codec.FLOAT.optionalFieldOf("bounce_energy", 0.9f).forGetter(Motion::bounceEnergy),
                Codec.BOOL.optionalFieldOf("die_on_collision", true).forGetter(Motion::dieOnCollision)
        ).apply(instance, Motion::new));
    }

    public record Lifetime(float ticks, float randomTicks) implements EntityComponent {
        @Override
        public @NonNull String type() {
            return "lifetime";
        }

        public static final Codec<Lifetime> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("ticks").forGetter(Lifetime::ticks),
                Codec.FLOAT.optionalFieldOf("random_ticks", 0f).forGetter(Lifetime::randomTicks)
        ).apply(instance, Lifetime::new));
    }

    public record Damage(float amount, DamageKind kind, float knockback) implements EntityComponent {
        @Override
        public @NonNull String type() {
            return "damage";
        }

        public static final Codec<Damage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("amount").forGetter(Damage::amount),
                DamageKind.CODEC.optionalFieldOf("kind", DamageKind.PROJECTILE).forGetter(Damage::kind),
                Codec.FLOAT.optionalFieldOf("knockback", 0f).forGetter(Damage::knockback)
        ).apply(instance, Damage::new));
    }

    public record Explosion(float radius, float damage) implements EntityComponent {
        @Override
        public @NonNull String type() {
            return "explosion";
        }

        public static final Codec<Explosion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("radius").forGetter(Explosion::radius),
                Codec.FLOAT.optionalFieldOf("damage", 0f).forGetter(Explosion::damage)
        ).apply(instance, Explosion::new));
    }

    public record Render(Identifier texture, boolean additive, float scale) implements EntityComponent {
        @Override
        public @NonNull String type() {
            return "render";
        }

        public static final Codec<Render> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Render::texture),
                Codec.BOOL.optionalFieldOf("additive", false).forGetter(Render::additive),
                Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(Render::scale)
        ).apply(instance, Render::new));
    }
}
