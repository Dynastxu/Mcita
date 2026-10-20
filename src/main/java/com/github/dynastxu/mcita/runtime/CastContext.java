package com.github.dynastxu.mcita.runtime;

import com.github.dynastxu.mcita.core.action.ShotConfig;
import com.github.dynastxu.mcita.core.action.StatOp;
import com.github.dynastxu.mcita.core.projectile.ProjectileProfile;
import com.github.dynastxu.mcita.core.projectile.TriggerSpec;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * What an {@link com.github.dynastxu.mcita.core.action.ActionOp} is allowed to do.
 * Implemented by the cast session; kept as an interface so the data layer never
 * depends on the runtime.
 */
public interface CastContext {
    ShotConfig shot();

    void spawnProjectile(Holder<ProjectileProfile> projectile, int count, Optional<TriggerSpec> trigger);

    void drawActions(int count, boolean instantReload);

    void addReload(StatOp op);

    void addRecoil(float amount);

    void custom(Identifier behavior, CompoundTag params);
}
