package com.github.dynastxu.mcita.core.action;

import com.mojang.serialization.Codec;

import java.util.Locale;

/** Mirrors {@code data/scripts/gun/gun_enums.lua}. Order matters for imports. */
public enum ActionType {
    PROJECTILE,
    STATIC_PROJECTILE,
    MODIFIER,
    DRAW_MANY,
    MATERIAL,
    OTHER,
    UTILITY,
    PASSIVE;

    public static final Codec<ActionType> CODEC = Codec.STRING.xmap(
            name -> valueOf(name.toUpperCase(Locale.ROOT)),
            type -> type.name().toLowerCase(Locale.ROOT));
}
