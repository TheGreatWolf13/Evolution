package tgw.evolution.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public abstract class CreatureEntity extends net.minecraft.entity.CreatureEntity {

    protected static final DataParameter<Boolean> DEAD = EntityDataManager.createKey(CreatureEntity.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Boolean> SKELETON = EntityDataManager.createKey(CreatureEntity.class, DataSerializers.BOOLEAN);
    protected int deathTimer;

    protected CreatureEntity(EntityType<? extends CreatureEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /**
     * Sets whether the entity is in the 'dead state' or not.
     */
    public void death() {
        this.setInvulnerable(true);
        this.setHealth(this.getMaxHealth());
        this.setPose(Pose.DYING);
        for (int k = 0; k < 20; ++k) {
            double d2 = this.rand.nextGaussian() * 0.02D;
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            this.world.addParticle(ParticleTypes.POOF, this.posX + this.rand.nextFloat() * this.getWidth() * 2.0F - this.getWidth(), this.posY + this.rand.nextFloat() * this.getHeight(), this.posZ + this.rand.nextFloat() * this.getWidth() * 2.0F - this.getWidth(), d2, d0, d1);
        }
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
    public boolean canBeAttackedWithItem() {
        return !this.dataManager.get(DEAD);
    }

    /**
     * @return The time it takes for the entity's body to become a 'skeleton'.
     */
    public abstract int skeletonTime();

    /**
     * @return Whether this entity is in the 'dead state'.
     */
    public boolean isDead() {
        return this.dataManager.get(DEAD);
    }

    @Override
    public Direction getBedDirection() {
        return Direction.UP;
    }

    /**
     * Gets the time the entity has been dead for in ticks.
     */
    public int getDeathTime() {
        return this.deathTimer;
    }

    public boolean isSkeleton() {
        return this.dataManager.get(SKELETON);
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(DEAD, false);
        this.dataManager.register(SKELETON, false);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("DeathTimer", this.deathTimer);
        compound.putBoolean("Dead", this.dataManager.get(DEAD));
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.deathTimer = compound.getInt("DeathTimer");
        this.dataManager.set(DEAD, compound.getBoolean("Dead"));
    }

    @Override
    public void livingTick() {
        super.livingTick();
        if (this.isDead()) {
            this.deathTimer++;
            if (this.deathTimer == 1) {
                this.setMotion(0, 0, 0);
                this.navigator.clearPath();
            }
            if (this.deathTimer >= this.skeletonTime()) {
                this.dataManager.set(SKELETON, true);
            }
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        if (ForgeHooks.onLivingDeath(this, cause)) {
            return;
        }
        if (!this.isDead()) {
            Entity entity = cause.getTrueSource();
            if (entity != null) {
                entity.onKillEntity(this);
            }
            this.getCombatTracker().reset();
            this.world.setEntityState(this, (byte) 3);
            this.dataManager.set(DEAD, true);
            this.death();
        }
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }
}
