package tgw.evolution.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.util.EntityStates;

public abstract class EntityGenericCreature extends CreatureEntity implements IEntityMass {

    protected static final DataParameter<Boolean> DEAD = EntityDataManager.createKey(EntityGenericCreature.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Boolean> SKELETON = EntityDataManager.createKey(EntityGenericCreature.class, DataSerializers.BOOLEAN);
    protected int deathTimer;

    protected EntityGenericCreature(EntityType<? extends EntityGenericCreature> type, World worldIn) {
        super(type, worldIn);
    }

    /**
     * @return Whether this entity becomes a skeleton after {@link EntityGenericCreature#skeletonTime()} ticks or simply disappears.
     */
    public abstract boolean becomesSkeleton();

    @Override
    public boolean canBeAttackedWithItem() {
        return !this.dataManager.get(DEAD);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }

    public abstract float getBaseHealth();

    public abstract float getBaseWalkForce();

    @Override
    public Direction getBedDirection() {
        return Direction.UP;
    }

    /**
     * Gets the time the entity has been dead for, in ticks.
     */
    public int getDeathTime() {
        return this.deathTimer;
    }

    @Override
    protected int getExperiencePoints(PlayerEntity player) {
        return 0;
    }

    public abstract float getFrictionModifier();

    /**
     * @return The leg height of the entity in m.
     */
    public abstract double getLegHeight();

    public abstract double getLegSlowDown();

    /**
     * @return Whether this entity is in the 'dead state'.
     */
    public boolean isDead() {
        return this.dataManager.get(DEAD);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return this.isInvulnerable() && !source.canHarmInCreative();
    }

    public boolean isSkeleton() {
        return this.dataManager.get(SKELETON);
    }

    /**
     * Kills this entity.
     */
    public void kill() {
        this.dataManager.set(DEAD, true);
        this.setInvulnerable(true);
        this.setHealth(this.getMaxHealth());
        this.setPose(Pose.DYING);
        this.spawnExplosionParticle();
    }

    @Override
    public void livingTick() {
        super.livingTick();
        //Start of my livingTick
        if (this.isDead()) {
            this.deathTimer++;
            if (this.deathTimer == 1) {
                this.setMotion(0, this.getMotion().y, 0);
                this.navigator.clearPath();
            }
            if (!this.isSkeleton() && this.skeletonTime() > 0) {
                if (this.deathTimer >= this.skeletonTime()) {
                    if (this.becomesSkeleton()) {
                        this.dataManager.set(SKELETON, true);
                    }
                    else {
                        this.remove();
                    }
                }
            }
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        if (ForgeHooks.onLivingDeath(this, cause)) {
            return;
        }
        if (cause == EvolutionDamage.VOID) {
            if (!this.dead) {
                LivingEntity attackingEntity = this.getAttackingEntity();
                if (this.scoreValue >= 0 && attackingEntity != null) {
                    attackingEntity.awardKillScore(this, this.scoreValue, cause);
                }
                Entity entity = cause.getTrueSource();
                if (entity != null) {
                    entity.onKillEntity(this);
                }
                if (this.isSleeping()) {
                    this.wakeUp();
                }
                this.dead = true;
                this.getCombatTracker().reset();
                this.world.setEntityState(this, EntityStates.DEATH_SOUND);
                this.setPose(Pose.DYING);
            }
            return;
        }
        if (!this.isDead()) {
            LivingEntity attackingEntity = this.getAttackingEntity();
            if (this.scoreValue >= 0 && attackingEntity != null) {
                attackingEntity.awardKillScore(this, this.scoreValue, cause);
            }
            Entity trueSource = cause.getTrueSource();
            if (trueSource != null) {
                trueSource.onKillEntity(this);
            }
            this.getCombatTracker().reset();
            this.world.setEntityState(this, EntityStates.DEATH_SOUND);
            this.kill();
        }
    }

    @Override
    public void onKillCommand() {
        this.attackEntityFrom(EvolutionDamage.VOID, Float.MAX_VALUE);
    }

    @Override
    public boolean preventDespawn() {
        return true;
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.deathTimer = compound.getInt("DeathTimer");
        this.dataManager.set(DEAD, compound.getBoolean("Dead"));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getBaseHealth());
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getBaseWalkForce());
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(DEAD, false);
        this.dataManager.register(SKELETON, false);
    }

    /**
     * @return The time it takes for the entity's body to become a 'skeleton', in ticks.
     * If {@code 0}, the entity does not turn into skeleton.
     */
    public abstract int skeletonTime();

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("DeathTimer", this.deathTimer);
        compound.putBoolean("Dead", this.dataManager.get(DEAD));
    }
}
