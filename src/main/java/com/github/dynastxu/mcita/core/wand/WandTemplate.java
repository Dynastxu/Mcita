package com.github.dynastxu.mcita.core.wand;

import com.github.dynastxu.mcita.McitaRegistries;
import com.github.dynastxu.mcita.core.action.Action;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;

import java.util.List;

/**
 * Datapack template for a preset wand: the stats shown on the tooltip plus the
 * deck the wand starts with. A random wand does not use this record; it builds
 * a {@link WandStats} snapshot directly, which is then stored on the instance.
 */
public record WandTemplate(WandStats stats, List<Holder<Action>> deck) {
    public WandTemplate {
        deck = List.copyOf(deck);
    }

    public static final Codec<WandTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandStats.CODEC.fieldOf("stats").forGetter(WandTemplate::stats),
            RegistryFixedCodec.create(McitaRegistries.ACTION).listOf()
                    .optionalFieldOf("deck", List.of()).forGetter(WandTemplate::deck)
    ).apply(instance, WandTemplate::new));
}
