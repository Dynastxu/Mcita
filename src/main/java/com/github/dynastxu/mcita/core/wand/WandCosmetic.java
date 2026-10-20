package com.github.dynastxu.mcita.core.wand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * Cosmetic half of a wand. The Noita source pairs each sprite with grip/tip offsets;
 * the same fields are kept so the generated table imports directly.
 */
public record WandCosmetic(
        Identifier model,
        String nameKey,
        float gripX,
        float gripY,
        float tipX,
        float tipY
) {
    public static final Codec<WandCosmetic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("model").forGetter(WandCosmetic::model),
            Codec.STRING.fieldOf("name").forGetter(WandCosmetic::nameKey),
            Codec.FLOAT.optionalFieldOf("grip_x", 0f).forGetter(WandCosmetic::gripX),
            Codec.FLOAT.optionalFieldOf("grip_y", 0f).forGetter(WandCosmetic::gripY),
            Codec.FLOAT.optionalFieldOf("tip_x", 0f).forGetter(WandCosmetic::tipX),
            Codec.FLOAT.optionalFieldOf("tip_y", 0f).forGetter(WandCosmetic::tipY)
    ).apply(instance, WandCosmetic::new));
}
