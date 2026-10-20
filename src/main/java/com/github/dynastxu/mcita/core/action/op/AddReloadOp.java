package com.github.dynastxu.mcita.core.action.op;

import com.github.dynastxu.mcita.core.action.ActionOp;
import com.github.dynastxu.mcita.core.action.StatOp;
import com.github.dynastxu.mcita.runtime.CastContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** {@code current_reload_time = ...}, applied when this shot finishes. */
public record AddReloadOp(StatOp op) implements ActionOp {
    public static final MapCodec<AddReloadOp> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StatOp.CODEC.fieldOf("op").forGetter(AddReloadOp::op)
    ).apply(instance, AddReloadOp::new));

    @Override
    public String type() {
        return "add_reload";
    }

    @Override
    public void apply(CastContext context) {
        context.addReload(op);
    }
}
