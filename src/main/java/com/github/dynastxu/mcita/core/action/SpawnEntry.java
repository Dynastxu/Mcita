package com.github.dynastxu.mcita.core.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** One row of Noita's parallel {@code spawn_level} / {@code spawn_probability} arrays. */
public record SpawnEntry(int level, float weight) {
    public static final Codec<SpawnEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(SpawnEntry::level),
            Codec.FLOAT.fieldOf("weight").forGetter(SpawnEntry::weight)
    ).apply(instance, SpawnEntry::new));
}
