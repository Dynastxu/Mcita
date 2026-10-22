package com.github.dynastxu.mcita.core.projectile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * Data for one projectile, the port of a Noita projectile entity XML.
 * All numbers are in Minecraft units (blocks, ticks) after datagen conversion.
 */
public record ProjectileProfile(
        Motion motion,
        Lifetime lifetime,
        Damage damage,
        Optional<Explosion> explosion,
        Optional<TriggerSpec> trigger,
        Optional<Render> render
) {
    public static final Codec<ProjectileProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Motion.CODEC.fieldOf("motion").forGetter(ProjectileProfile::motion),
            Lifetime.CODEC.fieldOf("lifetime").forGetter(ProjectileProfile::lifetime),
            Damage.CODEC.fieldOf("damage").forGetter(ProjectileProfile::damage),
            Explosion.CODEC.optionalFieldOf("explosion").forGetter(ProjectileProfile::explosion),
            TriggerSpec.CODEC.optionalFieldOf("trigger").forGetter(ProjectileProfile::trigger),
            Render.CODEC.optionalFieldOf("render").forGetter(ProjectileProfile::render)
    ).apply(instance, ProjectileProfile::new));

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
    ) {
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

    public record Lifetime(float ticks, float randomTicks) {
        public static final Codec<Lifetime> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("ticks").forGetter(Lifetime::ticks),
                Codec.FLOAT.optionalFieldOf("random_ticks", 0f).forGetter(Lifetime::randomTicks)
        ).apply(instance, Lifetime::new));
    }

    public record Damage(float amount, DamageKind kind, float knockback) {
        public static final Codec<Damage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("amount").forGetter(Damage::amount),
                DamageKind.CODEC.optionalFieldOf("kind", DamageKind.PROJECTILE).forGetter(Damage::kind),
                Codec.FLOAT.optionalFieldOf("knockback", 0f).forGetter(Damage::knockback)
        ).apply(instance, Damage::new));
    }

    public record Explosion(float radius, float damage) {
        public static final Codec<Explosion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("radius").forGetter(Explosion::radius),
                Codec.FLOAT.optionalFieldOf("damage", 0f).forGetter(Explosion::damage)
        ).apply(instance, Explosion::new));
    }

    public record Render(Identifier texture, boolean additive, float scale) {
        public static final Codec<Render> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Render::texture),
                Codec.BOOL.optionalFieldOf("additive", false).forGetter(Render::additive),
                Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(Render::scale)
        ).apply(instance, Render::new));
    }
}
