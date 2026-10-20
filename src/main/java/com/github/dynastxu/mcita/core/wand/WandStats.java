package com.github.dynastxu.mcita.core.wand;

import com.github.dynastxu.mcita.McitaRegistries;
import com.github.dynastxu.mcita.core.action.Action;
import com.github.dynastxu.mcita.core.action.ShotStat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;

import java.util.List;
import java.util.Map;

/**
 * Immutable wand template: {@code AbilityComponent} + {@code gun_config} +
 * {@code gunaction_config} from the Noita wand XML. Instances that need per-wand
 * random stats will later wrap this and override {@link #shotDefaults()}.
 */
public record WandStats(
        int deckCapacity,
        int actionsPerRound,
        float reloadTicks,
        boolean shuffleWhenEmpty,
        float manaMax,
        float manaChargeSpeed,
        List<Holder<Action>> alwaysCast,
        Map<ShotStat, Float> shotDefaults,
        WandCosmetic cosmetic
) {
    public WandStats {
        alwaysCast = List.copyOf(alwaysCast);
        shotDefaults = Map.copyOf(shotDefaults);
    }

    public static final Codec<WandStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("deck_capacity").forGetter(WandStats::deckCapacity),
            Codec.INT.optionalFieldOf("actions_per_round", 1).forGetter(WandStats::actionsPerRound),
            Codec.FLOAT.optionalFieldOf("reload_ticks", 0f).forGetter(WandStats::reloadTicks),
            Codec.BOOL.optionalFieldOf("shuffle_when_empty", false).forGetter(WandStats::shuffleWhenEmpty),
            Codec.FLOAT.optionalFieldOf("mana_max", 0f).forGetter(WandStats::manaMax),
            Codec.FLOAT.optionalFieldOf("mana_charge_speed", 0f).forGetter(WandStats::manaChargeSpeed),
            RegistryFixedCodec.create(McitaRegistries.ACTION).listOf()
                    .optionalFieldOf("always_cast", List.<Holder<Action>>of()).forGetter(WandStats::alwaysCast),
            Codec.unboundedMap(ShotStat.CODEC, Codec.FLOAT)
                    .optionalFieldOf("shot_defaults", Map.<ShotStat, Float>of()).forGetter(WandStats::shotDefaults),
            WandCosmetic.CODEC.fieldOf("cosmetic").forGetter(WandStats::cosmetic)
    ).apply(instance, WandStats::new));
}
