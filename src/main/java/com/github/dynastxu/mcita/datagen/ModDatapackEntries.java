package com.github.dynastxu.mcita.datagen;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.McitaRegistries;
import com.github.dynastxu.mcita.core.action.Action;
import com.github.dynastxu.mcita.core.action.ActionType;
import com.github.dynastxu.mcita.core.action.ShotStat;
import com.github.dynastxu.mcita.core.action.SpawnEntry;
import com.github.dynastxu.mcita.core.action.StatOp;
import com.github.dynastxu.mcita.core.action.ShotPatch;
import com.github.dynastxu.mcita.core.action.UsesLogic;
import com.github.dynastxu.mcita.core.action.op.AddRecoilOp;
import com.github.dynastxu.mcita.core.action.op.CustomOp;
import com.github.dynastxu.mcita.core.action.op.DrawActionsOp;
import com.github.dynastxu.mcita.core.action.op.PatchShotOp;
import com.github.dynastxu.mcita.core.action.op.SpawnProjectileOp;
import com.github.dynastxu.mcita.core.projectile.DamageKind;
import com.github.dynastxu.mcita.core.projectile.ProjectileProfile;
import com.github.dynastxu.mcita.core.wand.WandCosmetic;
import com.github.dynastxu.mcita.core.wand.WandStats;
import com.github.dynastxu.mcita.core.wand.WandTemplate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hand-written entries for the first vertical slice. The bulk importer will replace
 * these later; the shapes must not change.
 */
public final class ModDatapackEntries {
    private ModDatapackEntries() {
    }

    public static RegistrySetBuilder build() {
        return new RegistrySetBuilder()
                .add(McitaRegistries.PROJECTILE, ModDatapackEntries::projectiles)
                .add(McitaRegistries.ACTION, ModDatapackEntries::actions)
                .add(McitaRegistries.WAND, ModDatapackEntries::wands);
    }

    private static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String name) {
        return ResourceKey.create(registry, Mcita.id(name));
    }

    // ---------------------------------------------------------------- projectiles

    private static void projectiles(BootstrapContext<ProjectileProfile> context) {
        context.register(key(McitaRegistries.PROJECTILE, "light_bullet"), new ProjectileProfile(
                new ProjectileProfile.Motion(
                        NoitaValues.speed(750), NoitaValues.speed(850),
                        NoitaValues.gravity(200),
                        1.7f, 0.04f, 1f, 0, true),
                new ProjectileProfile.Lifetime(NoitaValues.ticks(40), NoitaValues.ticks(7)),
                new ProjectileProfile.Damage(NoitaValues.damage(0.12), DamageKind.PROJECTILE, 0f),
                Optional.empty(),
                Optional.empty(),
                Optional.of(new ProjectileProfile.Render(Mcita.id("textures/entity/buckshot"), true, 1f))
        ));

        context.register(key(McitaRegistries.PROJECTILE, "bomb"), new ProjectileProfile(
                new ProjectileProfile.Motion(
                        NoitaValues.speed(300), NoitaValues.speed(400),
                        NoitaValues.gravity(200),
                        1.2f, 0.1f, 0.98f, 0, true),
                new ProjectileProfile.Lifetime(NoitaValues.ticks(200), NoitaValues.ticks(10)),
                new ProjectileProfile.Damage(NoitaValues.damage(0.1), DamageKind.PROJECTILE, 0f),
                Optional.of(new ProjectileProfile.Explosion(
                        NoitaValues.blocks(60), NoitaValues.damage(3.0))),
                Optional.empty(),
                Optional.of(new ProjectileProfile.Render(Mcita.id("textures/entity/rubber_ball"), false, 1f))
        ));
    }

    // -------------------------------------------------------------------- actions

    private static void actions(BootstrapContext<Action> context) {
        HolderGetter<ProjectileProfile> projectiles = context.lookup(McitaRegistries.PROJECTILE);
        Holder<ProjectileProfile> lightBullet = projectiles.getOrThrow(key(McitaRegistries.PROJECTILE, "light_bullet"));
        Holder<ProjectileProfile> bomb = projectiles.getOrThrow(key(McitaRegistries.PROJECTILE, "bomb"));

        context.register(key(McitaRegistries.ACTION, "light_bullet"), new Action(
                ActionType.PROJECTILE,
                5, Action.INFINITE_USES, 100,
                List.of(new SpawnEntry(0, 2f), new SpawnEntry(1, 1f), new SpawnEntry(2, 0.5f)),
                Optional.of(Mcita.id("textures/item/spell_light_bullet")),
                Optional.of(Mcita.id("textures/item/spell_light_bullet")),
                UsesLogic.DEFAULT,
                List.of(
                        new SpawnProjectileOp(lightBullet, Optional.empty(), 1),
                        new PatchShotOp(ShotPatch.of(Map.of(
                                ShotStat.FIRE_RATE_WAIT, StatOp.add(NoitaValues.ticks(3)),
                                ShotStat.SCREEN_SHAKE, StatOp.add(0.5f),
                                ShotStat.SPREAD_DEGREES, StatOp.add(-1f),
                                ShotStat.CRITICAL_CHANCE, StatOp.add(5f))))
                )));

        context.register(key(McitaRegistries.ACTION, "bomb"), new Action(
                ActionType.PROJECTILE,
                25, 3, 200,
                List.of(new SpawnEntry(0, 1f), new SpawnEntry(1, 1f), new SpawnEntry(2, 1f)),
                Optional.of(Mcita.id("textures/item/spell_nuke")),
                Optional.of(Mcita.id("textures/item/spell_nuke")),
                UsesLogic.DEFAULT,
                List.of(
                        new SpawnProjectileOp(bomb, Optional.empty(), 1),
                        new PatchShotOp(ShotPatch.of(Map.of(
                                ShotStat.FIRE_RATE_WAIT, StatOp.add(NoitaValues.ticks(100))))),
                        new AddRecoilOp(23f)
                )));

        context.register(key(McitaRegistries.ACTION, "burst_2"), new Action(
                ActionType.DRAW_MANY,
                0, Action.INFINITE_USES, 140,
                List.of(new SpawnEntry(0, 0.8f), new SpawnEntry(1, 0.8f)),
                Optional.of(Mcita.id("textures/item/spell_burst_2")),
                Optional.of(Mcita.id("textures/item/spell_burst_2")),
                UsesLogic.DEFAULT,
                List.of(new DrawActionsOp(2, true))));

        context.register(key(McitaRegistries.ACTION, "critical_hit"), new Action(
                ActionType.MODIFIER,
                0, Action.INFINITE_USES, 150,
                List.of(new SpawnEntry(1, 1f), new SpawnEntry(2, 1f)),
                Optional.of(Mcita.id("textures/item/spell_critical_hit")),
                Optional.of(Mcita.id("textures/item/spell_critical_hit")),
                UsesLogic.DEFAULT,
                List.of(
                        new DrawActionsOp(1, false),
                        new PatchShotOp(ShotPatch.of(Map.of(
                                ShotStat.CRITICAL_CHANCE, StatOp.add(5f),
                                ShotStat.FIRE_RATE_WAIT, StatOp.add(NoitaValues.ticks(10))))))));

        context.register(key(McitaRegistries.ACTION, "add_trigger"), new Action(
                ActionType.OTHER,
                10, Action.INFINITE_USES, 100,
                List.of(new SpawnEntry(3, 0.3f), new SpawnEntry(4, 0.6f)),
                Optional.of(Mcita.id("textures/item/spell_light_bullet_trigger")),
                Optional.of(Mcita.id("textures/item/spell_light_bullet_trigger")),
                UsesLogic.DEFAULT,
                List.of(new CustomOp(Mcita.id("add_trigger"), new CompoundTag()))));
    }

    // ---------------------------------------------------------------------- wands

    private static void wands(BootstrapContext<WandTemplate> context) {
        HolderGetter<Action> actions = context.lookup(McitaRegistries.ACTION);
        Holder<Action> lightBullet = actions.getOrThrow(key(McitaRegistries.ACTION, "light_bullet"));
        Holder<Action> bomb = actions.getOrThrow(key(McitaRegistries.ACTION, "bomb"));
        Holder<Action> burst2 = actions.getOrThrow(key(McitaRegistries.ACTION, "burst_2"));
        Holder<Action> criticalHit = actions.getOrThrow(key(McitaRegistries.ACTION, "critical_hit"));

        // Starter preset: the same three cards the old hardcoded starter deck used.
        context.register(key(McitaRegistries.WAND, "starting_wand"), new WandTemplate(
                new WandStats(
                        3, 1, NoitaValues.ticks(24), false, 100f, 30f,
                        List.of(lightBullet, lightBullet),
                        Map.of(ShotStat.FIRE_RATE_WAIT, NoitaValues.ticks(10)),
                        new WandCosmetic(
                                Mcita.id("wand_lc_sc_s"),
                                "wand.mcita.bolt_staff",
                                2f, 5f, 21f, 5f)),
                List.of(lightBullet, bomb, burst2)));

        context.register(key(McitaRegistries.WAND, "spread_staff"), new WandTemplate(
                new WandStats(
                        5, 3, NoitaValues.ticks(36), true, 120f, 40f,
                        List.of(),
                        Map.of(
                                ShotStat.FIRE_RATE_WAIT, NoitaValues.ticks(8),
                                ShotStat.SPREAD_DEGREES, 8f),
                        new WandCosmetic(
                                Mcita.id("wand_mlc_3c_s"),
                                "wand.mcita.spread_staff",
                                2f, 5f, 21f, 5f)),
                List.of(lightBullet, lightBullet, burst2)));

        context.register(key(McitaRegistries.WAND, "destruction_staff"), new WandTemplate(
                new WandStats(
                        4, 1, NoitaValues.ticks(90), false, 150f, 20f,
                        List.of(bomb),
                        Map.of(
                                ShotStat.FIRE_RATE_WAIT, NoitaValues.ticks(20),
                                ShotStat.SPREAD_DEGREES, 2f),
                        new WandCosmetic(
                                Mcita.id("wand_of_destruction"),
                                "wand.mcita.destruction_staff",
                                2f, 5f, 21f, 5f)),
                List.of(bomb, lightBullet, criticalHit, burst2)));

        context.register(key(McitaRegistries.WAND, "quick_staff"), new WandTemplate(
                new WandStats(
                        4, 1, NoitaValues.ticks(12), true, 80f, 60f,
                        List.of(lightBullet),
                        Map.of(
                                ShotStat.FIRE_RATE_WAIT, NoitaValues.ticks(3),
                                ShotStat.SPREAD_DEGREES, 3f),
                        new WandCosmetic(
                                Mcita.id("wand_smc_sc_ns"),
                                "wand.mcita.quick_staff",
                                2f, 5f, 21f, 5f)),
                List.of(lightBullet, lightBullet, criticalHit)));
    }
}
