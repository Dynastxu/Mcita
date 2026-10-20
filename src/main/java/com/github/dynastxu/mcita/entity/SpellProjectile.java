package com.github.dynastxu.mcita.entity;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.McitaRegistries;
import com.github.dynastxu.mcita.core.action.ShotConfig;
import com.github.dynastxu.mcita.core.action.ShotStat;
import com.github.dynastxu.mcita.core.projectile.ProjectileProfile;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * One generic projectile entity driven by a {@link ProjectileProfile}. The profile id
 * is synced so the client can resolve the registry entry and pick its texture; the
 * per-shot config stays server-side because only damage/motion use it.
 */
public class SpellProjectile extends Projectile {
    private static final EntityDataAccessor<String> DATA_PROFILE =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);

    private ProjectileProfile profile;
    private ShotConfig shot;
    private float remainingLife;

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }

    public void configure(Holder<ProjectileProfile> holder, ShotConfig shot) {
        this.profile = holder.value();
        this.shot = shot;
        this.remainingLife = profile.lifetime().ticks() + this.random.nextFloat() * profile.lifetime().randomTicks();
        holder.unwrapKey()
                .map(key -> key.identifier().toString())
                .ifPresent(id -> this.entityData.set(DATA_PROFILE, id));
    }

    public ProjectileProfile getProfile() {
        if (this.profile == null) {
            String id = this.entityData.get(DATA_PROFILE);
            if (!id.isEmpty()) {
                Identifier identifier = Identifier.tryParse(id);
                if (identifier != null) {
                    this.profile = this.level().registryAccess()
                            .lookupOrThrow(McitaRegistries.PROJECTILE)
                            .get(identifier)
                            .map(holder -> holder.value())
                            .orElse(null);
                }
            }
        }
        return this.profile;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_PROFILE, "");
    }

    @Override
    public void tick() {
        super.tick();

        ProjectileProfile profile = getProfile();
        if (profile == null) {
            if (!this.level().isClientSide()) {
                discard();
            }
            return;
        }

        if (this.remainingLife-- <= 0) {
            explodeOrDiscard();
            return;
        }

        Vec3 delta = getDeltaMovement();
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
            return;
        }

        setPos(getX() + delta.x, getY() + delta.y, getZ() + delta.z);
        if (profile.motion().gravity() != 0) {
            setDeltaMovement(delta.x, delta.y - profile.motion().gravity(), delta.z);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide()) {
            return;
        }

        ProjectileProfile profile = getProfile();
        if (profile != null && result instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity target) {
            float bonus = this.shot == null ? 0f : this.shot.get(ShotStat.DAMAGE_PROJECTILE_ADD);
            float damage = Math.max(0f, profile.damage().amount() + bonus);
            if (damage > 0f) {
                target.hurtServer((ServerLevel) this.level(),
                        this.level().damageSources().thrown(this, getOwner()), damage);
            }
        }
        explodeOrDiscard();
    }

    private void explodeOrDiscard() {
        ProjectileProfile profile = getProfile();
        if (this.level() instanceof ServerLevel server && profile != null && profile.explosion().isPresent()) {
            ProjectileProfile.Explosion explosion = profile.explosion().get();
            if (explosion.radius() > 0f) {
                server.explode(this, getX(), getY(), getZ(), explosion.radius(), Level.ExplosionInteraction.NONE);
            }
        } else if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 4, 0.1, 0.1, 0.1, 0.01);
        }
        discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // Projectiles are short-lived; profiles are re-applied on spawn.
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putString("profile", this.entityData.get(DATA_PROFILE));
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
