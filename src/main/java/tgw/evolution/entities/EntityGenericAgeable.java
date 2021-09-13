package tgw.evolution.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionParticles;

public abstract class EntityGenericAgeable<T extends EntityGenericAgeable<T>> extends EntityGenericCreature<T> {

    private static final DataParameter<Integer> AGE = EntityDataManager.defineId(EntityGenericAgeable.class, DataSerializers.INT);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.defineId(EntityGenericAgeable.class, DataSerializers.BOOLEAN);
    protected int sleepTime;
    private int lifeSpan;
    private boolean slept;

    protected EntityGenericAgeable(EntityType<T> type, World world) {
        super(type, world);
        this.lifeSpan = this.computeLifeSpan();
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Age", this.entityData.get(AGE));
        compound.putInt("LifeSpan", this.lifeSpan);
        compound.putBoolean("Sleeping", this.entityData.get(SLEEPING));
        compound.putBoolean("Slept", this.slept);
        compound.putInt("SleepTime", this.sleepTime);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.isDead()) {
            this.entityData.set(AGE, this.entityData.get(AGE) + 1);
            if (this.lifeSpan > 0 && this.entityData.get(AGE) > this.lifeSpan) {
                this.kill();
                return;
            }
            if (this.isSleeping()) {
                if (this.level.isClientSide) {
                    if (this.tickCount % 100 == 0) {
                        for (int i = 0; i < 3; i++) {
                            this.level.addParticle(EvolutionParticles.SLEEP.get(),
                                                   this.getX() + this.random.nextFloat() * this.getBbWidth() * 2 - this.getBbWidth(),
                                                   this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                                                   this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2 - this.getBbWidth(),
                                                   this.random.nextGaussian() * 0.02,
                                                   this.random.nextGaussian() * 0.02,
                                                   this.random.nextGaussian() * 0.02);
                        }
                    }
                }
            }
            if (this.level.getDayTime() % 24_000 == 0) {
                this.slept = false;
            }
        }
    }

    /**
     * Calculates this entity's lifespan in ticks. If {@code 0}, this entity lives forever and will not die of old age.
     */
    public abstract int computeLifeSpan();

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AGE, 0);
        this.entityData.define(SLEEPING, false);
    }

    /**
     * Sets the age at which the entity is considered to be an adult in ticks.
     */
    public abstract int getAdultAge();

    /**
     * Returns the age of the entity in ticks.
     */
    public int getAge() {
        return this.entityData.get(AGE);
    }

    /**
     * Returns the lifespan the entity will live for in ticks.
     */
    public int getLifeSpan() {
        return this.lifeSpan;
    }

    /**
     * Sets the age at which the entity is considered to be old in ticks.
     */
    public int getOldAge() {
        return 3 * this.lifeSpan / 4;
    }

    /**
     * @return Returns whether this entity has fulfilled its sleep quota for the day.
     */
    public boolean hasSlept() {
        return this.slept;
    }

    /**
     * Returns whether this entity is an adult or not.
     */
    public boolean isAdult() {
        return !this.isBaby() && !this.isOld();
    }

    /**
     * Returns whether this entity is a child or not.
     */
    @Override
    public boolean isBaby() {
        return this.getAdultAge() > this.entityData.get(AGE);
    }

    /**
     * Returns whether this entity is old or not.
     */
    public boolean isOld() {
        return this.entityData.get(AGE) > this.getOldAge();
    }

    @Override
    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    /**
     * @return The child mortally rate of this entity. Values must range from {@code 0f} to {@code 1f}.
     */
    public abstract float mortallyRate();

    @Override
    public void onSyncedDataUpdated(DataParameter<?> key) {
        if (AGE.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(AGE, compound.getInt("Age"));
        this.lifeSpan = compound.getInt("LifeSpan");
        this.setSleeping(compound.getBoolean("Sleeping"));
        this.slept = compound.getBoolean("Slept");
        this.sleepTime = compound.getInt("SleepTime");
    }

    public void setAge(int age) {
        this.entityData.set(AGE, age);
    }

    public void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
        if (this.isDead()) {
            this.setPose(Pose.DYING);
        }
        else if (sleeping) {
            this.setPose(Pose.SLEEPING);
        }
        else {
            this.setPose(Pose.STANDING);
        }
    }

    public void setSlept() {
        this.slept = true;
    }

    @Override
    public void stopSleeping() {
        //Override of the method because of a call in LivingEntity.class which wanted a valid bed
    }
}