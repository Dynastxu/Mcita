package com.github.dynastxu.mcita.core.action.op;

import com.github.dynastxu.mcita.core.action.ActionOp;
import com.github.dynastxu.mcita.runtime.CastContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** {@code shot_effects.recoil_knockback += n}. */
public record AddRecoilOp(float amount) implements ActionOp {
    public static final MapCodec<AddRecoilOp> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("amount").forGetter(AddRecoilOp::amount)
    ).apply(instance, AddRecoilOp::new));

    @Override
    public String type() {
        return "add_recoil";
    }

    @Override
    public void apply(CastContext context) {
        context.addRecoil(amount);
    }
}
