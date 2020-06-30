package tgw.evolution.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionParticles;

import javax.annotation.Nullable;

public abstract class AgeableEntity extends CreatureEntity {

    private static final DataParameter<Boolean> CHILD = EntityDataManager.createKey(AgeableEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(AgeableEntity.class, DataSerializers.BOOLEAN);
    private int age;
    private int lifeSpan;
    public boolean slept;
    protected int sleepTime;

    protected AgeableEntity(EntityType<? extends AgeableEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /**
     * Sets the lifespan of the entity in ticks.
     */
    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    /**
     * Returns the lifespan the entity will live for in ticks.
     */
    public int getDeterminedLifeSpan() {
        return this.lifeSpan;
    }

    /**
     * Sets the age at which the entity is considered to be an adult in ticks.
     */
    public abstract int getAdultAge();

    /**
     * Sets the age at which the entity is considered to be old in ticks.
     */
    public abstract int getOldAge();

    /**
     * Returns the lifespan of the entity in ticks.
     */
    public abstract int getLifeSpan();

    @Nullable
    public abstract AgeableEntity createChild(AgeableEntity ageable);

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(CHILD, false);
        this.dataManager.register(SLEEPING, false);
    }

    /**
     * Returns the age of the entity in ticks.
     */
    public int getAge() {
        return this.age;
    }

    /**
     * Sets the entity age in ticks.
     */
    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public boolean isSleeping() {
        return this.dataManager.get(SLEEPING);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("Age", this.age);
        compound.putInt("LifeSpan", this.lifeSpan);
        compound.putBoolean("Sleeping", this.dataManager.get(SLEEPING));
        compound.putBoolean("Slept", this.slept);
        compound.putInt("SleepTime", this.sleepTime);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.age = compound.getInt("Age");
        this.lifeSpan = compound.getInt("LifeSpan");
        this.setSleeping(compound.getBoolean("Sleeping"));
        this.slept = compound.getBoolean("Slept");
        this.sleepTime = compound.getInt("SleepTime");
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (CHILD.equals(key)) {
            this.recalculateSize();
        }
        super.notifyDataManagerChange(key);
    }

    public void setSleeping(boolean sleeping) {
        this.dataManager.set(SLEEPING, sleeping);
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

    @Override
    public void wakeUp() {
        //Override of the method because of a call in LivingEntity.class which wanted a valid bed
    }

    @Override
    public void livingTick() {
        super.livingTick();
        if (!this.isDead()) {
            this.age++;
            if (this.age > this.lifeSpan) {
                this.dataManager.set(DEAD, true);
                this.death();
            }
            if (this.isSleeping()) {
                if (this.world.isRemote) {
                    if (this.age % 100 == 0) {
                        for (int i = 0; i < 3; i++) {
                            double d0 = this.rand.nextGaussian() * 0.02D;
                            double d1 = this.rand.nextGaussian() * 0.02D;
                            double d2 = this.rand.nextGaussian() * 0.02D;
                            this.world.addParticle(EvolutionParticles.SLEEP.get(), this.posX + this.rand.nextFloat() * this.getWidth() * 2.0F - this.getWidth(), this.posY + 0.5D + this.rand.nextFloat() * this.getHeight(), this.posZ + this.rand.nextFloat() * this.getWidth() * 2.0F - this.getWidth(), d0, d1, d2);
                        }
                    }
                }
            }
            if (this.world.getDayTime() % 24000 == 0) {
                this.slept = false;
            }
        }
    }

    /**
     * Returns whether this entity is a child or not.
     */
    @Override
    public boolean isChild() {
        return this.getAdultAge() > this.age;
    }

    /**
     * Returns whether this entity is old or not.
     */
    public boolean isOld() {
        return this.age > this.getOldAge();
    }

    /**
     * Returns whether this entity is an adult or not.
     */
    public boolean isAdult() {
        return !this.isChild() && !this.isOld();
    }
}