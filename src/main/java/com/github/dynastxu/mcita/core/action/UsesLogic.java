package com.github.dynastxu.mcita.core.action;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum UsesLogic {
    /** Consume one use when the card is played and the shot produced something. */
    DEFAULT,
    /** Never consume uses. */
    NEVER,
    /** The card itself decides (see the custom op). */
    CUSTOM;

    public static final Codec<UsesLogic> CODEC = Codec.STRING.xmap(
            name -> valueOf(name.toUpperCase(Locale.ROOT)),
            logic -> logic.name().toLowerCase(Locale.ROOT));
}
