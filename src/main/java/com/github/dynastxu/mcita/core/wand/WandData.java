package com.github.dynastxu.mcita.core.wand;

import com.github.dynastxu.mcita.McitaRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistent wand state: deck contents, pile order, mana and cooldown deadlines.
 * Cooldowns are stored as absolute game times so nothing has to tick every frame.
 */
public record WandData(
        Holder<WandStats> stats,
        Map<Integer, ActionCard> cards,
        List<Integer> drawPile,
        List<Integer> discardPile,
        float mana,
        long lastManaTick,
        long nextCastTick,
        long nextReloadTick,
        boolean reloading
) {
    public WandData {
        cards = Map.copyOf(cards);
        drawPile = List.copyOf(drawPile);
        discardPile = List.copyOf(discardPile);
    }

    /**
     * NBT (used by component sync and saving) only supports string map keys, so the
     * slot -> card map is serialized with string keys. An int-keyed map fails with
     * "Not a string" at encode time.
     */
    private static final Codec<Map<Integer, ActionCard>> CARDS_CODEC =
            Codec.unboundedMap(Codec.STRING, ActionCard.CODEC).xmap(
                    raw -> {
                        Map<Integer, ActionCard> cards = new HashMap<>();
                        raw.forEach((key, card) -> cards.put(Integer.parseInt(key), card));
                        return cards;
                    },
                    cards -> {
                        Map<String, ActionCard> raw = new HashMap<>();
                        cards.forEach((slot, card) -> raw.put(Integer.toString(slot), card));
                        return raw;
                    });

    public static final Codec<WandData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryFixedCodec.create(McitaRegistries.WAND).fieldOf("stats").forGetter(WandData::stats),
            CARDS_CODEC.optionalFieldOf("cards", Map.of()).forGetter(WandData::cards),
            Codec.INT.listOf().optionalFieldOf("draw", List.of()).forGetter(WandData::drawPile),
            Codec.INT.listOf().optionalFieldOf("discard", List.of()).forGetter(WandData::discardPile),
            Codec.FLOAT.optionalFieldOf("mana", 0f).forGetter(WandData::mana),
            Codec.LONG.optionalFieldOf("last_mana_tick", 0L).forGetter(WandData::lastManaTick),
            Codec.LONG.optionalFieldOf("next_cast_tick", 0L).forGetter(WandData::nextCastTick),
            Codec.LONG.optionalFieldOf("next_reload_tick", 0L).forGetter(WandData::nextReloadTick),
            Codec.BOOL.optionalFieldOf("reloading", false).forGetter(WandData::reloading)
    ).apply(instance, WandData::new));

    public WandData withState(Map<Integer, ActionCard> cards, List<Integer> drawPile,
                              List<Integer> discardPile, float mana, long lastManaTick,
                              long nextCastTick, long nextReloadTick, boolean reloading) {
        return new WandData(stats, cards, drawPile, discardPile, mana, lastManaTick,
                nextCastTick, nextReloadTick, reloading);
    }
}
