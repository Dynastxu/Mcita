package com.github.dynastxu.mcita.core.action;

import com.github.dynastxu.mcita.runtime.CastContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

/**
 * One step of a spell's behaviour. Data-driven so a new spell needs no Java code,
 * while exotic cards can be registered as {@link com.github.dynastxu.mcita.core.action.op.CustomOp}.
 *
 * <p>Deliberately not {@code sealed}: the concrete ops live in a subpackage, and a
 * sealed interface in the unnamed module requires all permitted subclasses in the
 * same package. The {@code type} discriminator plus {@link ActionOps} provides the
 * same closed-set behaviour without that restriction.
 */
public interface ActionOp {
    String type();

    void apply(CastContext context);

    Codec<ActionOp> CODEC = Codec.STRING.partialDispatch("type",
            op -> DataResult.success(op.type()),
            type -> {
                MapCodec<? extends ActionOp> codec = ActionOps.codec(type);
                return codec == null
                        ? DataResult.error(() -> "Unknown action op type: " + type)
                        : DataResult.success(codec);
            });
}
