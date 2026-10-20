package com.github.dynastxu.mcita.core.action;

import com.mojang.serialization.Codec;

import java.util.Map;

/** A batch of {@link ShotStat} mutations, applied in declaration order by index. */
public record ShotPatch(Map<ShotStat, StatOp> stats) {
    public static final Codec<ShotPatch> CODEC =
            Codec.unboundedMap(ShotStat.CODEC, StatOp.CODEC).xmap(ShotPatch::new, ShotPatch::stats);

    public static ShotPatch of(Map<ShotStat, StatOp> stats) {
        return new ShotPatch(Map.copyOf(stats));
    }
}
