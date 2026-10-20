package com.github.dynastxu.mcita.core.action.op;

import com.github.dynastxu.mcita.core.action.ActionOp;
import com.github.dynastxu.mcita.core.action.ShotPatch;
import com.github.dynastxu.mcita.runtime.CastContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Mutates the current shot config, i.e. {@code c.x = c.x + n}. */
public record PatchShotOp(ShotPatch patch) implements ActionOp {
    public static final MapCodec<PatchShotOp> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ShotPatch.CODEC.fieldOf("patch").forGetter(PatchShotOp::patch)
    ).apply(instance, PatchShotOp::new));

    @Override
    public String type() {
        return "patch_shot";
    }

    @Override
    public void apply(CastContext context) {
        context.shot().apply(patch);
    }
}
