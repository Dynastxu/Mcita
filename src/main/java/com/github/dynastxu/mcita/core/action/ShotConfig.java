package com.github.dynastxu.mcita.core.action;

import com.github.dynastxu.mcita.core.wand.WandStats;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.EnumMap;
import java.util.Map;

/**
 * Mutable per-shot state, the equivalent of Noita's {@code c} table.
 * One instance is created per shot and shared by every projectile of that shot.
 */
public final class ShotConfig {
    private final float[] values = new float[ShotStat.values().length];
    private boolean friendlyFire;

    public ShotConfig() {
        for (ShotStat stat : ShotStat.values()) {
            values[stat.ordinal()] = stat.defaultValue();
        }
    }

    public float get(ShotStat stat) {
        return values[stat.ordinal()];
    }

    public void set(ShotStat stat, float value) {
        values[stat.ordinal()] = value;
    }

    public boolean friendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public void apply(ShotPatch patch) {
        patch.stats().forEach((stat, op) -> set(stat, op.apply(get(stat))));
    }

    /** Only values differing from the stat default, so generated JSON stays readable. */
    public Map<ShotStat, Float> nonDefaultValues() {
        Map<ShotStat, Float> map = new EnumMap<>(ShotStat.class);
        for (ShotStat stat : ShotStat.values()) {
            float value = values[stat.ordinal()];
            if (value != stat.defaultValue()) {
                map.put(stat, value);
            }
        }
        return map;
    }

    public ShotConfig copy() {
        ShotConfig copy = new ShotConfig();
        System.arraycopy(values, 0, copy.values, 0, values.length);
        copy.friendlyFire = friendlyFire;
        return copy;
    }

    /** Initial shot state for a wand: its {@code gunaction_config} defaults. */
    public static ShotConfig of(WandStats stats) {
        ShotConfig config = new ShotConfig();
        stats.shotDefaults().forEach(config::set);
        return config;
    }

    public static ShotConfig fromMap(Map<ShotStat, Float> map, boolean friendlyFire) {
        ShotConfig config = new ShotConfig();
        map.forEach(config::set);
        config.friendlyFire = friendlyFire;
        return config;
    }

    public static final Codec<ShotConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ShotStat.CODEC, Codec.FLOAT)
                    .optionalFieldOf("values", Map.<ShotStat, Float>of())
                    .forGetter(ShotConfig::nonDefaultValues),
            Codec.BOOL.optionalFieldOf("friendly_fire", false).forGetter(ShotConfig::friendlyFire)
    ).apply(instance, ShotConfig::fromMap));
}
