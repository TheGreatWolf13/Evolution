package tgw.evolution.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionParticles;

public abstract class EntityGenericAgeable extends EntityGenericCreature {

    private static final DataParameter<Integer> AGE = EntityDataManager.createKey(EntityGenericAgeable.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(EntityGenericAgeable.class, DataSerializers.BOOLEAN);
    protected int sleepTime;
    private boolean slept;
    private int lifeSpan;

    protected EntityGenericAgeable(EntityType<? extends EntityGenericAgeable> type, World worldIn) {
        super(type, worldIn);
        this.lifeSpan = this.computeLifeSpan();
    }

    /**
     * Calculates this entity's lifespan in ticks. If {@code 0}, this entity lives forever and will not die of old age.
     */
    public abstract int computeLifeSpan();

    /**
     * @return Returns whether this entity has fulfilled its sleep quota for the day.
     */
    public boolean hasSlept() {
        return this.slept;
    }

    public void setSlept() {
        this.slept = true;
    }

    /**
     * @return The child mortally rate of this entity. Values must range from {@code 0f} to {@code 1f}.
     */
    public abstract float mortallyRate();

    /**
     * Returns the lifespan the entity will live for in ticks.
     */
    public int getLifeSpan() {
        return this.lifeSpan;
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(AGE, 0);
        this.dataManager.register(SLEEPING, false);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("Age", this.dataManager.get(AGE));
        compound.putInt("LifeSpan", this.lifeSpan);
        compound.putBoolean("Sleeping", this.dataManager.get(SLEEPING));
        compound.putBoolean("Slept", this.slept);
        compound.putInt("SleepTime", this.sleepTime);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.dataManager.set(AGE, compound.getInt("Age"));
        this.lifeSpan = compound.getInt("LifeSpan");
        this.setSleeping(compound.getBoolean("Sleeping"));
        this.slept = compound.getBoolean("Slept");
        this.sleepTime = compound.getInt("SleepTime");
    }

    @Override
    public void livingTick() {
        super.livingTick();
        if (!this.isDead()) {
            this.dataManager.set(AGE, this.dataManager.get(AGE) + 1);
            if (this.lifeSpan > 0 && this.dataManager.get(AGE) > this.lifeSpan) {
                this.kill();
                return;
            }
            if (this.isSleeping()) {
                if (this.world.isRemote) {
                    if (this.ticksExisted % 100 == 0) {
                        for (int i = 0; i < 3; i++) {
                            this.world.addParticle(EvolutionParticles.SLEEP.get(),
                                                   this.posX + this.rand.nextFloat() * this.getWidth() * 2.0F - this.getWidth(),
                                                   this.posY + 0.5D + this.rand.nextFloat() * this.getHeight(),
                                                   this.posZ + this.rand.nextFloat() * this.getWidth() * 2.0F - this.getWidth(),
                                                   this.rand.nextGaussian() * 0.02,
                                                   this.rand.nextGaussian() * 0.02,
                                                   this.rand.nextGaussian() * 0.02);
                        }
                    }
                }
            }
            if (this.world.getDayTime() % 24_000 == 0) {
                this.slept = false;
            }
        }
    }

    /**
     * Returns the age of the entity in ticks.
     */
    public int getAge() {
        return this.dataManager.get(AGE);
    }

    public void setAge(int age) {
        this.dataManager.set(AGE, age);
    }

    /**
     * Returns whether this entity is an adult or not.
     */
    public boolean isAdult() {
        return !this.isChild() && !this.isOld();
    }

    /**
     * Returns whether this entity is a child or not.
     */
    @Override
    public boolean isChild() {
        return this.getAdultAge() > this.dataManager.get(AGE);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (AGE.equals(key)) {
            this.recalculateSize();
        }
        super.notifyDataManagerChange(key);
    }

    @Override
    public boolean isSleeping() {
        return this.dataManager.get(SLEEPING);
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

    /**
     * Returns whether this entity is old or not.
     */
    public boolean isOld() {
        return this.dataManager.get(AGE) > this.getOldAge();
    }

    /**
     * Sets the age at which the entity is considered to be an adult in ticks.
     */
    public abstract int getAdultAge();

    /**
     * Sets the age at which the entity is considered to be old in ticks.
     */
    public int getOldAge() {
        return 3 * this.lifeSpan / 4;
    }
}