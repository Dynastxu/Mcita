package com.github.dynastxu.mcita.entity;

import com.github.dynastxu.mcita.McitaRegistries;
import com.github.dynastxu.mcita.core.action.ShotConfig;
import com.github.dynastxu.mcita.core.action.ShotStat;
import com.github.dynastxu.mcita.core.component.EntityContext;
import com.github.dynastxu.mcita.core.component.projectile.ProjectileSystems;
import com.github.dynastxu.mcita.core.projectile.DamageKind;
import com.github.dynastxu.mcita.core.projectile.ProjectileProfile;
import net.minecraft.core.Holder;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

/**
 * One generic projectile entity driven by a {@link ProjectileProfile}. The profile id
 * is synced so the client can resolve the registry entry and pick its texture; the
 * per-shot config stays server-side because only damage/motion use it.
 *
 * <p>This class owns no behaviour. Each tick it hands itself to
 * {@link ProjectileSystems#PIPELINE} as an {@link EntityContext}, and the systems run
 * only for the components the profile actually carries. Adding a projectile behaviour
 * therefore means adding a component and a system, never editing this class.
 *
 * <p>Motion is integrated on the server only: the client interpolates the synced
 * position, so bounces and explosions cannot desync between the two sides.
 */
public class SpellProjectile extends Projectile implements EntityContext {
    private static final EntityDataAccessor<String> DATA_PROFILE =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);

    private ProjectileProfile profile;
    private ShotConfig shot;
    private float remainingLife;
    private int remainingBounces;
    private boolean stuck;
    private boolean dead;

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
        this.dead = false;
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

        ProjectileSystems.PIPELINE.tick(this, profile);
    }

    // ---------------------------------------------------------- EntityContext

    @Override
    public Entity entity() {
        return this;
    }

    @Override
    public void setPosition(Vec3 position) {
        setPos(position.x, position.y, position.z);
    }

    @Override
    public Vec3 velocity() {
        return getDeltaMovement();
    }

    @Override
    public void setVelocity(Vec3 velocity) {
        setDeltaMovement(velocity);
    }

    @Override
    public boolean inLiquid() {
        return isInLiquid();
    }

    @Override
    public float remainingLife() {
        return this.remainingLife;
    }

    @Override
    public void setRemainingLife(float ticks) {
        this.remainingLife = ticks;
    }

    @Override
    public int remainingBounces() {
        return this.remainingBounces;
    }

    @Override
    public void setRemainingBounces(int bounces) {
        this.remainingBounces = bounces;
    }

    @Override
    public boolean stuck() {
        return this.stuck;
    }

    @Override
    public void setStuck(boolean stuck) {
        this.stuck = stuck;
    }

    @Override
    public boolean alive() {
        return !this.dead;
    }

    @Override
    public void despawn() {
        this.dead = true;
        discard();
    }

    @Override
    public HitResult raycastMove() {
        return ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
    }

    @Override
    public boolean canHit(Entity target) {
        return canHitEntity(target);
    }

    @Override
    public void hurtTarget(LivingEntity target, float amount, DamageKind kind) {
        if (this.level() instanceof ServerLevel server) {
            target.hurtServer(server, this.level().damageSources().thrown(this, getOwner()), amount);
        }
    }

    @Override
    public void explode(float radius) {
        if (this.level() instanceof ServerLevel server) {
            server.explode(this, getX(), getY(), getZ(), radius, Level.ExplosionInteraction.NONE);
        }
    }

    @Override
    public float shotValue(ShotStat stat) {
        return this.shot == null ? 0f : this.shot.get(stat);
    }

    // ------------------------------------------------------------ MC overrides

    @Override
    protected boolean canHitEntity(@NonNull Entity entity) {
        // Noita's projectile_player shots pass through their caster unless friendly fire is on.
        if (entity == this.getOwner() && (this.shot == null || !this.shot.friendlyFire())) {
            return false;
        }
        return super.canHitEntity(entity);
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
