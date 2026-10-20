package com.github.dynastxu.mcita.core.projectile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;

/**
 * Deferred card draw attached to a projectile, the port of
 * {@code add_projectile_trigger_*(file, n)}. When the trigger fires, the owning
 * cast session draws {@code drawCount} more cards with {@code instantReload = true}.
 */
public record TriggerSpec(TriggerMode mode, float delayTicks, int drawCount) {
    public enum TriggerMode {
        HIT_WORLD,
        HIT_ENEMY,
        DEATH,
        TIMER;

        public static final Codec<TriggerMode> CODEC = Codec.STRING.xmap(
                name -> valueOf(name.toUpperCase(Locale.ROOT)),
                mode -> mode.name().toLowerCase(Locale.ROOT));
    }

    public static final Codec<TriggerSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TriggerMode.CODEC.fieldOf("mode").forGetter(TriggerSpec::mode),
            Codec.FLOAT.optionalFieldOf("delay_ticks", 0f).forGetter(TriggerSpec::delayTicks),
            Codec.INT.optionalFieldOf("draw_count", 1).forGetter(TriggerSpec::drawCount)
    ).apply(instance, TriggerSpec::new));
}
