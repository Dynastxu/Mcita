package com.github.dynastxu.mcita.core.action.op;

import com.github.dynastxu.mcita.core.action.ActionOp;
import com.github.dynastxu.mcita.runtime.CastContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** {@code draw_actions(n, instant_reload)}: multicasts and "draw one more" modifiers. */
public record DrawActionsOp(int count, boolean instantReload) implements ActionOp {
    public static final MapCodec<DrawActionsOp> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("count").forGetter(DrawActionsOp::count),
            Codec.BOOL.optionalFieldOf("instant_reload", false).forGetter(DrawActionsOp::instantReload)
    ).apply(instance, DrawActionsOp::new));

    @Override
    public String type() {
        return "draw_actions";
    }

    @Override
    public void apply(CastContext context) {
        context.drawActions(count, instantReload);
    }
}
