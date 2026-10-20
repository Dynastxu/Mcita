package com.github.dynastxu.mcita.core.projectile;

import com.mojang.serialization.Codec;

import java.util.Locale;

/** Noita's {@code DAMAGE_*} families; mapped to MC damage types later. */
public enum DamageKind {
    PROJECTILE,
    EXPLOSION,
    MELEE,
    ELECTRICITY,
    FIRE,
    DRILL,
    SLICE,
    ICE,
    CURSE,
    HEALING,
    PHYSICS_IMPACT,
    RADIOACTIVE,
    POISON,
    HOLY,
    SUFFOCATION,
    DROWNING,
    MATERIAL;

    public static final Codec<DamageKind> CODEC = Codec.STRING.xmap(
            name -> valueOf(name.toUpperCase(Locale.ROOT)),
            kind -> kind.name().toLowerCase(Locale.ROOT));
}
