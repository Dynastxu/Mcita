package com.github.dynastxu.mcita.core.action;

import com.mojang.serialization.Codec;

import java.util.Locale;

/**
 * The numeric part of Noita's per-shot config ({@code c} in {@code gun.lua}).
 * All values are already in Minecraft units (ticks / blocks / damage); conversion
 * happens once in datagen.
 */
public enum ShotStat {
    FIRE_RATE_WAIT(0f),
    SPEED_MULTIPLIER(1f),
    CHILD_SPEED_MULTIPLIER(1f),
    DAMPENING(1f),
    EXPLOSION_RADIUS(0f),
    SPREAD_DEGREES(0f),
    PATTERN_DEGREES(0f),
    SCREEN_SHAKE(0f),
    RECOIL(0f),
    DAMAGE_MELEE_ADD(0f),
    DAMAGE_PROJECTILE_ADD(0f),
    DAMAGE_ELECTRICITY_ADD(0f),
    DAMAGE_FIRE_ADD(0f),
    DAMAGE_EXPLOSION_ADD(0f),
    DAMAGE_ICE_ADD(0f),
    DAMAGE_SLICE_ADD(0f),
    DAMAGE_HEALING_ADD(0f),
    DAMAGE_CURSE_ADD(0f),
    DAMAGE_DRILL_ADD(0f),
    DAMAGE_NULL_ALL(0f),
    CRITICAL_CHANCE(0f),
    CRITICAL_MULTIPLIER(0f),
    EXPLOSION_DAMAGE_TO_MATERIALS(0f),
    KNOCKBACK_FORCE(0f),
    LIGHTNING_COUNT(0f),
    MATERIAL_AMOUNT(0f),
    TRAIL_MATERIAL_AMOUNT(0f),
    BOUNCES(0f),
    GRAVITY(0f),
    LIGHT(0f),
    BLOOD_COUNT_MULTIPLIER(1f),
    GORE_PARTICLES(0f),
    RAGDOLL_FX(0f),
    PHYSICS_IMPULSE_COEFF(0f),
    LIFETIME_ADD(0f);

    private final float defaultValue;

    ShotStat(float defaultValue) {
        this.defaultValue = defaultValue;
    }

    public float defaultValue() {
        return defaultValue;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ShotStat byKey(String key) {
        for (ShotStat stat : values()) {
            if (stat.key().equals(key)) {
                return stat;
            }
        }
        throw new IllegalArgumentException("Unknown shot stat: " + key);
    }

    public static final Codec<ShotStat> CODEC = Codec.STRING.xmap(ShotStat::byKey, ShotStat::key);
}
