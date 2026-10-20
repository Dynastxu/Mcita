package com.github.dynastxu.mcita.core.action.op;

import com.github.dynastxu.mcita.core.action.ActionOp;
import com.github.dynastxu.mcita.runtime.CastContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

/**
 * Escape hatch for cards whose Lua body manipulates the deck or needs engine access
 * (ADD_TRIGGER, TAU, wand refresh, ...). The behaviour is a registered code handler.
 */
public record CustomOp(Identifier behavior, CompoundTag params) implements ActionOp {
    public static final MapCodec<CustomOp> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("behavior").forGetter(CustomOp::behavior),
            CompoundTag.CODEC.optionalFieldOf("params", new CompoundTag()).forGetter(CustomOp::params)
    ).apply(instance, CustomOp::new));

    @Override
    public String type() {
        return "custom";
    }

    @Override
    public void apply(CastContext context) {
        context.custom(behavior, params);
    }
}
