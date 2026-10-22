package com.github.dynastxu.mcita.entity;

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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

/**
 * One generic projectile entity driven by a {@link ProjectileProfile}. The profile id
 * is synced so the client can resolve the registry entry and pick its texture; the
 * per-shot config stays server-side because only damage/motion use it.
 *
 * <p>Motion is integrated on the server only: the client interpolates the synced
 * position, so bounces and explosions cannot desync between the two sides.
 */
public class SpellProjectile extends Projectile {
    private static final EntityDataAccessor<String> DATA_PROFILE =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);

    private ProjectileProfile profile;
    private ShotConfig shot;
    private float remainingLife;
    private int remainingBounces;
    private boolean stuck;

    private final InterpolationHandler interpolationHandler = new InterpolationHandler(this);

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolationHandler;
    }

    public void configure(Holder<ProjectileProfile> holder, ShotConfig shot) {
        this.profile = holder.value();
        this.shot = shot;
        this.remainingLife = this.profile.lifetime().ticks()
                + this.random.nextFloat() * this.profile.lifetime().randomTicks();
        this.remainingBounces = Math.max(0, this.profile.motion().bounces());
        this.stuck = false;
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
                            .map(Holder.Reference::value)
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
        this.interpolationHandler.interpolate();
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        ProjectileProfile profile = getProfile();
        if (profile == null) {
            discard();
            return;
        }

        if (this.remainingLife-- <= 0) {
            explodeOrDiscard();
            return;
        }

        if (this.stuck) {
            return;
        }

        Vec3 delta = getDeltaMovement();
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
            return;
        }

        setPos(getX() + delta.x, getY() + delta.y, getZ() + delta.z);
        applyPhysics(profile.motion());
    }

    /**
     * Applies the per-tick part of Noita's {@code VelocityComponent}: air drag,
     * fluid drag and gravity. This mirrors the physics of the previous, field-tested
     * NoitaCore build exactly.
     *
     * <p>{@code air_friction} is a per-frame coefficient (0 = vacuum, larger = more
     * drag, negative = thrust, as used by Accelerating Shot), so 3 frames per tick
     * means cubing the per-frame factor. Inside a liquid Noita switches to quadratic
     * drag instead, scaled by the same coefficient.
     */
    private void applyPhysics(ProjectileProfile.Motion motion) {
        Vec3 velocity = getDeltaMovement();
        float airFriction = motion.airFriction();

        if (this.isInLiquid()) {
            float drag = airFriction / 10.5f;
            velocity = new Vec3(
                    velocity.x - drag * velocity.x * Math.abs(velocity.x),
                    velocity.y - drag * velocity.y * Math.abs(velocity.y),
                    velocity.z - drag * velocity.z * Math.abs(velocity.z));
        } else if (airFriction != 0f) {
            float perFrame = Math.max(0f, 1f - airFriction / 60f);
            velocity = velocity.scale(perFrame * perFrame * perFrame);
        }

        if (motion.gravity() != 0f) {
            velocity = new Vec3(velocity.x, velocity.y - motion.gravity(), velocity.z);
        }
        setDeltaMovement(velocity);
    }

    @Override
    protected void onHit(@NonNull HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide()) {
            return;
        }

        ProjectileProfile profile = getProfile();
        if (profile == null) {
            discard();
            return;
        }

        // Explosions and damage must happen where the projectile actually hit, not
        // where it started this tick.
        Vec3 impact = result.getLocation();
        setPos(impact.x, impact.y, impact.z);

        if (result instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity target) {
            applyDamage(profile, target);
        }

        if (result instanceof BlockHitResult blockHit) {
            if (tryBounce(profile.motion(), blockHit)) {
                return;
            }
            if (!profile.motion().dieOnCollision()) {
                // on_collision_die = 0: stop against the surface, lifetime still runs out.
                this.stuck = true;
                setDeltaMovement(Vec3.ZERO);
                return;
            }
        }

        explodeOrDiscard();
    }

    private void applyDamage(ProjectileProfile profile, LivingEntity target) {
        float bonus = this.shot == null ? 0f : this.shot.get(ShotStat.DAMAGE_PROJECTILE_ADD);
        float damage = Math.max(0f, profile.damage().amount() + bonus);
        if (damage > 0f) {
            target.hurtServer((ServerLevel) this.level(),
                    this.level().damageSources().thrown(this, getOwner()), damage);
        }
    }

    private boolean tryBounce(ProjectileProfile.Motion motion, BlockHitResult hit) {
        if (this.remainingBounces <= 0) {
            return false;
        }
        this.remainingBounces--;

        Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getUnitVec3i());
        Vec3 velocity = getDeltaMovement();
        double dot = velocity.dot(normal);
        if (dot < 0) {
            velocity = velocity.subtract(normal.scale(2 * dot));
        }
        setDeltaMovement(velocity.scale(Math.max(0f, motion.bounceEnergy())));
        // Step off the face so next tick's ray does not start inside the block.
        setPos(getX() + normal.x * 0.05, getY() + normal.y * 0.05, getZ() + normal.z * 0.05);
        return true;
    }

    @Override
    protected boolean canHitEntity(@NonNull Entity entity) {
        // Noita's projectile_player shots pass through their caster unless friendly fire is on.
        if (entity == this.getOwner() && (this.shot == null || !this.shot.friendlyFire())) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    private void explodeOrDiscard() {
        ProjectileProfile profile = getProfile();
        if (this.level() instanceof ServerLevel server && profile != null
                && profile.explosion().isPresent()) {
            ProjectileProfile.Explosion explosion = profile.explosion().get();
            if (explosion.radius() > 0f) {
                server.explode(this, getX(), getY(), getZ(), explosion.radius(),
                        Level.ExplosionInteraction.NONE);
            }
        } else if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 4, 0.1, 0.1, 0.1, 0.01);
        }
        discard();
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_PROFILE, input.getStringOr("profile", ""));
        this.remainingLife = input.getFloatOr("life", 0f);
        this.remainingBounces = input.getIntOr("bounces", 0);
        this.stuck = input.getBooleanOr("stuck", false);
        this.shot = input.read("shot", ShotConfig.CODEC).orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("profile", this.entityData.get(DATA_PROFILE));
        output.putFloat("life", this.remainingLife);
        output.putInt("bounces", this.remainingBounces);
        output.putBoolean("stuck", this.stuck);
        output.storeNullable("shot", ShotConfig.CODEC, this.shot);
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
