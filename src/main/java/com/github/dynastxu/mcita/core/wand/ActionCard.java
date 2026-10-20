package com.github.dynastxu.mcita.core.wand;

import com.github.dynastxu.mcita.McitaRegistries;
import com.github.dynastxu.mcita.core.action.Action;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;

/**
 * One physical card inside a wand. {@code usesRemaining == -1} means infinite and
 * {@code 0} means depleted: the two are different states, unlike the old model.
 */
public record ActionCard(Holder<Action> action, int usesRemaining) {
    public static final Codec<ActionCard> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryFixedCodec.create(McitaRegistries.ACTION).fieldOf("action").forGetter(ActionCard::action),
            Codec.INT.optionalFieldOf("uses", Action.INFINITE_USES).forGetter(ActionCard::usesRemaining)
    ).apply(instance, ActionCard::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ActionCard> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(McitaRegistries.ACTION), ActionCard::action,
            ByteBufCodecs.VAR_INT, ActionCard::usesRemaining,
            ActionCard::new);

    public boolean isInfinite() {
        return usesRemaining < 0;
    }

    public boolean isDepleted() {
        return usesRemaining == 0;
    }
}
