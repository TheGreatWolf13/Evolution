package tgw.evolution.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.util.EntityStates;

public abstract class EntityGenericCreature<T extends EntityGenericCreature<T>> extends CreatureEntity implements IEntityProperties,
                                                                                                                  IEvolutionEntity<T> {

    protected static final DataParameter<Boolean> DEAD = EntityDataManager.defineId(EntityGenericCreature.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Boolean> SKELETON = EntityDataManager.defineId(EntityGenericCreature.class, DataSerializers.BOOLEAN);
    protected int deathTimer;

    protected EntityGenericCreature(EntityType<T> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("DeathTimer", this.deathTimer);
        compound.putBoolean("Dead", this.entityData.get(DEAD));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isDead()) {
            this.deathTimer++;
            if (this.deathTimer == 1) {
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                this.navigation.stop();
            }
            if (!this.isSkeleton() && this.skeletonTime() > 0) {
                if (this.deathTimer >= this.skeletonTime()) {
                    if (this.becomesSkeleton()) {
                        this.entityData.set(SKELETON, true);
                    }
                    else {
                        this.remove();
                    }
                }
            }
        }
    }

    /**
     * @return Whether this entity becomes a skeleton after {@link EntityGenericCreature#skeletonTime()} ticks or simply disappears.
     */
    public abstract boolean becomesSkeleton();

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DEAD, false);
        this.entityData.define(SKELETON, false);
    }

    @Override
    public void die(DamageSource cause) {
        if (ForgeHooks.onLivingDeath(this, cause)) {
            return;
        }
        if (cause == EvolutionDamage.VOID) {
            if (!this.dead) {
                LivingEntity attackingEntity = this.getKillCredit();
                if (this.deathScore >= 0 && attackingEntity != null) {
                    attackingEntity.awardKillScore(this, this.deathScore, cause);
                }
                Entity entity = cause.getEntity();
                if (entity != null && this.level instanceof ServerWorld) {
                    entity.killed((ServerWorld) this.level, this);
                }
                if (this.isSleeping()) {
                    this.stopSleeping();
                }
                this.dead = true;
                this.getCombatTracker().recheckStatus();
                this.level.broadcastEntityEvent(this, EntityStates.DEATH_SOUND);
                this.setPose(Pose.DYING);
            }
            return;
        }
        if (!this.isDead()) {
            LivingEntity attackingEntity = this.getKillCredit();
            if (this.deathScore >= 0 && attackingEntity != null) {
                attackingEntity.awardKillScore(this, this.deathScore, cause);
            }
            Entity trueSource = cause.getEntity();
            if (trueSource != null && this.level instanceof ServerWorld) {
                trueSource.killed((ServerWorld) this.level, this);
            }
            this.getCombatTracker().recheckStatus();
            this.level.broadcastEntityEvent(this, EntityStates.DEATH_SOUND);
            this.kill();
        }
    }

    public abstract float getBaseHealth();

    public abstract float getBaseWalkForce();

    @Override
    public Direction getBedOrientation() {
        return Direction.UP;
    }

    /**
     * Gets the time the entity has been dead for, in ticks.
     */
    public int getDeathTime() {
        return this.deathTimer;
    }

    @Override
    protected int getExperienceReward(PlayerEntity player) {
        return 0;
    }

    @Override
    public abstract float getFrictionModifier();

    /**
     * @return The leg height of the entity in m.
     */
    public abstract double getLegHeight();

    @Override
    public boolean isAttackable() {
        return !this.entityData.get(DEAD);
    }

    /**
     * @return Whether this entity is in the 'dead state'.
     */
    public boolean isDead() {
        return this.entityData.get(DEAD);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return this.isInvulnerable() && !source.isCreativePlayer();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    public boolean isSkeleton() {
        return this.entityData.get(SKELETON);
    }

    @Override
    public void kill() {
        this.hurt(EvolutionDamage.VOID, Float.MAX_VALUE);
    }

    /**
     * Kills this entity.
     */
    public void onKilled() {
        this.entityData.set(DEAD, true);
        this.setInvulnerable(true);
        this.setHealth(this.getMaxHealth());
        this.setPose(Pose.DYING);
        for (int i = 0; i < 20; i++) {
            double dx = this.random.nextGaussian() * 0.02;
            double dy = this.random.nextGaussian() * 0.02;
            double dz = this.random.nextGaussian() * 0.02;
            this.level.addParticle(ParticleTypes.POOF, this.getRandomX(1), this.getRandomY(), this.getRandomZ(1), dx, dy, dz);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.deathTimer = compound.getInt("DeathTimer");
        this.entityData.set(DEAD, compound.getBoolean("Dead"));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    /**
     * @return The time it takes for the entity's body to become a 'skeleton', in ticks.
     * If {@code 0}, the entity does not turn into skeleton.
     */
    public abstract int skeletonTime();
}
