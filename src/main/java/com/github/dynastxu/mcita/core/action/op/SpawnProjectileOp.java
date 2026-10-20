package com.github.dynastxu.mcita.core.action.op;

import com.github.dynastxu.mcita.McitaRegistries;
import com.github.dynastxu.mcita.core.action.ActionOp;
import com.github.dynastxu.mcita.core.projectile.ProjectileProfile;
import com.github.dynastxu.mcita.core.projectile.TriggerSpec;
import com.github.dynastxu.mcita.runtime.CastContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;

import java.util.Optional;

/** {@code add_projectile} / {@code add_projectile_trigger_*}. */
public record SpawnProjectileOp(
        Holder<ProjectileProfile> projectile,
        Optional<TriggerSpec> trigger,
        int count
) implements ActionOp {
    public static final MapCodec<SpawnProjectileOp> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryFixedCodec.create(McitaRegistries.PROJECTILE).fieldOf("projectile").forGetter(SpawnProjectileOp::projectile),
            TriggerSpec.CODEC.optionalFieldOf("trigger").forGetter(SpawnProjectileOp::trigger),
            Codec.INT.optionalFieldOf("count", 1).forGetter(SpawnProjectileOp::count)
    ).apply(instance, SpawnProjectileOp::new));

    @Override
    public String type() {
        return "spawn_projectile";
    }

    @Override
    public void apply(CastContext context) {
        context.spawnProjectile(projectile, count, trigger);
    }
}
