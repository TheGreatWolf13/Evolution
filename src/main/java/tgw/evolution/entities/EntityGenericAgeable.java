//package tgw.evolution.entities;
//
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.syncher.EntityDataAccessor;
//import net.minecraft.network.syncher.EntityDataSerializers;
//import net.minecraft.network.syncher.SynchedEntityData;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.Pose;
//import net.minecraft.world.level.Level;
//
//public abstract class EntityGenericAgeable extends EntityGenericCreature {
//
//    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(EntityGenericAgeable.class, EntityDataSerializers.INT);
//    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(EntityGenericAgeable.class, EntityDataSerializers
//    .BOOLEAN);
//    protected int sleepTime;
//    private int lifeSpan;
//    private boolean slept;
//
//    protected EntityGenericAgeable(EntityType<? extends EntityGenericAgeable> type, Level level) {
//        super(type, level);
//        this.lifeSpan = this.computeLifeSpan();
//    }
//
//    @Override
//    public void addAdditionalSaveData(CompoundTag tag) {
//        super.addAdditionalSaveData(tag);
//        tag.putInt("Age", this.entityData.get(AGE));
//        tag.putInt("LifeSpan", this.lifeSpan);
//        tag.putBoolean("Sleeping", this.entityData.get(SLEEPING));
//        tag.putBoolean("Slept", this.slept);
//        tag.putInt("SleepTime", this.sleepTime);
//    }
//
//    @Override
//    public void aiStep() {
//        super.aiStep();
//        if (!this.isDead()) {
//            this.entityData.set(AGE, this.entityData.get(AGE) + 1);
//            if (this.lifeSpan > 0 && this.entityData.get(AGE) > this.lifeSpan) {
//                this.kill();
//                return;
//            }
//            if (this.isSleeping()) {
//                if (this.level.isClientSide) {
//                    if (this.tickCount % 100 == 0) {
//                        for (int i = 0; i < 3; i++) {
////                            this.level.addParticle(EvolutionParticles.SLEEP.get(),
////                                                   this.getX() + this.random.nextFloat() * this.getBbWidth() * 2 - this.getBbWidth(),
////                                                   this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
////                                                   this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2 - this.getBbWidth(),
////                                                   this.random.nextGaussian() * 0.02,
////                                                   this.random.nextGaussian() * 0.02,
////                                                   this.random.nextGaussian() * 0.02);
//                        }
//                    }
//                }
//            }
//            if (this.level.getDayTime() % 24_000 == 0) {
//                this.slept = false;
//            }
//        }
//    }
//
//    /**
//     * Calculates this entity's lifespan in ticks. If {@code 0}, this entity lives forever and will not die of old age.
//     */
//    public abstract int computeLifeSpan();
//
//    @Override
//    protected void defineSynchedData() {
//        super.defineSynchedData();
//        this.entityData.define(AGE, 0);
//        this.entityData.define(SLEEPING, false);
//    }
//
//    /**
//     * Sets the age at which the entity is considered to be an adult in ticks.
//     */
//    public abstract int getAdultAge();
//
//    /**
//     * Returns the age of the entity in ticks.
//     */
//    public int getAge() {
//        return this.entityData.get(AGE);
//    }
//
//    /**
//     * Returns the lifespan the entity will live for in ticks.
//     */
//    public int getLifeSpan() {
//        return this.lifeSpan;
//    }
//
//    /**
//     * Sets the age at which the entity is considered to be old in ticks.
//     */
//    public int getOldAge() {
//        return 3 * this.lifeSpan / 4;
//    }
//
//    /**
//     * @return Returns whether this entity has fulfilled its sleep quota for the day.
//     */
//    public boolean hasSlept() {
//        return this.slept;
//    }
//
//    /**
//     * Returns whether this entity is an adult or not.
//     */
//    public boolean isAdult() {
//        return !this.isBaby() && !this.isOld();
//    }
//
//    /**
//     * Returns whether this entity is a child or not.
//     */
//    @Override
//    public boolean isBaby() {
//        return this.getAdultAge() > this.entityData.get(AGE);
//    }
//
//    /**
//     * Returns whether this entity is old or not.
//     */
//    public boolean isOld() {
//        return this.entityData.get(AGE) > this.getOldAge();
//    }
//
//    @Override
//    public boolean isSleeping() {
//        return this.entityData.get(SLEEPING);
//    }
//
//    /**
//     * @return The child mortally rate of this entity. Values must range from {@code 0f} to {@code 1f}.
//     */
//    public abstract float mortallyRate();
//
//    @Override
//    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
//        if (AGE.equals(key)) {
//            this.refreshDimensions();
//        }
//        super.onSyncedDataUpdated(key);
//    }
//
//    @Override
//    public void readAdditionalSaveData(CompoundTag tag) {
//        super.readAdditionalSaveData(tag);
//        this.entityData.set(AGE, tag.getInt("Age"));
//        this.lifeSpan = tag.getInt("LifeSpan");
//        this.setSleeping(tag.getBoolean("Sleeping"));
//        this.slept = tag.getBoolean("Slept");
//        this.sleepTime = tag.getInt("SleepTime");
//    }
//
//    public void setAge(int age) {
//        this.entityData.set(AGE, age);
//    }
//
//    public void setSleeping(boolean sleeping) {
//        this.entityData.set(SLEEPING, sleeping);
//        if (this.isDead()) {
//            this.setPose(Pose.DYING);
//        }
//        else if (sleeping) {
//            this.setPose(Pose.SLEEPING);
//        }
//        else {
//            this.setPose(Pose.STANDING);
//        }
//    }
//
//    public void setSlept() {
//        this.slept = true;
//    }
//
//    @Override
//    public void stopSleeping() {
//        //Override of the method because of a call in LivingEntity.class which wanted a valid bed
//    }
//}