package com.github.dynastxu.mcita.registry;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.entity.SpellProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(Mcita.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SpellProjectile>> SPELL_PROJECTILE =
            ENTITIES.registerEntityType("spell_projectile", SpellProjectile::new, MobCategory.MISC,
                    builder -> builder
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(8)
                            .updateInterval(1));

    private ModEntities() {
    }
}
