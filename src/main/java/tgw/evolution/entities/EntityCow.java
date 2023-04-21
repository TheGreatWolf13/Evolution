package tgw.evolution.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.entities.ai.*;
import tgw.evolution.util.constants.EntityStates;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.time.Time;

public class EntityCow extends EntityGenericAnimal<EntityCow> {

    private int eatTimer;
    //    private EatGrassGoal eatGrassGoal;
    private GoalSleep goalSleep;
    private float tailIncX;
    private float tailIncZ;
    private int tailTimerX;
    private int tailTimerZ;
    public EntityCow(EntityType<EntityCow> type, Level level) {
        super(type, level);
        super.setAge(this.getAdultAge());
        if (!level.isClientSide) {
            this.goalSleep.setSleepTimer();
        }
    }

    @Override
    public void aiStep() {
        if (this.level.isClientSide) {
            this.eatTimer = Math.max(0, this.eatTimer - 1);
        }
        if (!this.level.isClientSide && this.level.getDayTime() % 24_000 == 0) {
            this.goalSleep.setSleepTimer();
        }
        super.aiStep();
    }

    @Override
    public void appendDebugInfo(MutableComponent text) {
        //TODO implementation
    }

    @Override
    public boolean becomesSkeleton() {
        return true;
    }

    @Override
    public boolean canBeInLove() {
        //TODO implementation
        return false;
    }

    @Override
    public int computeLifeSpan() {
        //TODO implementation
        return 4 * Time.TICKS_PER_YEAR;
    }

    @Override
    protected void customServerAiStep() {
        if (!this.isDead()) {
//            this.eatTimer = this.eatGrassGoal.getEatingGrassTimer();
            this.sleepTime = this.goalSleep.getSleepTimer();
        }
        super.customServerAiStep();
    }

    @Override
    public int getAdultAge() {
        return 2 * Time.TICKS_PER_YEAR;
    }

    @Override
    public double getBaseHealth() {
        //TODO implementation
        return 10;
    }

    @Override
    public double getBaseMass() {
        //TODO implementation
        return 700;
    }

    @Override
    public double getBaseWalkForce() {
        //TODO implementation
        return 0.025f;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        switch (pose) {
            case DYING -> {
                return EntityDimensions.scalable(1.2F, 0.7F).scale(this.getScale());
            }
            case SLEEPING -> {
                return EntityDimensions.scalable(0.9F, 0.7F).scale(this.getScale());
            }
        }
        return EntityDimensions.scalable(0.9F, 1.4F).scale(this.getScale());
    }

    @Override
    public float getFrictionModifier() {
        return 2.0f;
    }

    @Override
    public int getGestationPeriod() {
        return 9 * Time.TICKS_PER_MONTH;
    }

    public float getHeadRotationAngleX(float partialTicks) {
        if (this.hurtTime > 0) {
            this.eatTimer = 0;
            return 0.0F;
        }
        if (this.eatTimer > 4 && this.eatTimer <= 36) {
            float f = (this.eatTimer - 4 - partialTicks) / 32.0F;
            return Mth.PI / 5.0F + 0.219_911_49F * Mth.sin(f * 28.7F);
        }
        return this.eatTimer > 0 ? Mth.PI / 5.0F : Mth.DEG_TO_RAD * this.getXRot();
    }

    public float getHeadRotationPointY(float partialTicks) {
        if (this.hurtTime > 0) {
            this.eatTimer = 0;
            return 0.0F;
        }
        if (this.eatTimer <= 0) {
            return 0.0F;
        }
        if (this.eatTimer >= 4 && this.eatTimer <= 36) {
            return 1.0F;
        }
        return this.eatTimer < 4 ? (this.eatTimer - partialTicks) / 4.0F : -(this.eatTimer - 40 - partialTicks) / 4.0F;
    }

    @Override
    public @Nullable HitboxEntity<EntityCow> getHitboxes() {
        //TODO implementation
        return null;
    }

    @Override
    public double getLegHeight() {
        //TODO implementation
        return 0.8;
    }

    @Override
    public double getLegSlowdown() {
        //TODO implementation
        return 2.5 * this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public int getNumberOfBabies() {
        return this.random.nextInt(100) == 0 ? 2 : 1;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        if (pose == Pose.DYING) {
            return 0.3F;
        }
        if (pose == Pose.SLEEPING) {
            return 0.85F;
        }
        return this.isBaby() ? dimensions.height * 0.95F : 1.35F;
    }

    private boolean getTailChanceX() {
        if (this.isDead() || this.isSleeping()) {
            return false;
        }
        return this.random.nextInt(10_000) == 0;
    }

    private boolean getTailChanceZ() {
        if (this.isDead() || this.isSleeping()) {
            return false;
        }
        return this.random.nextInt(5_000) == 0;
    }

    @Override
    public double getVolume() {
        //TODO implementation
        return 0;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EntityStates.EAT_GRASS) {
            this.eatTimer = 40;
        }
        else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public float mortallyRate() {
        //TODO implementation
        return 0;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.goalSleep.setSleepTime(this.sleepTime);
    }

    @Override
    protected void registerGoals() {
//        this.eatGrassGoal = new EatGrassGoal(this);
        this.goalSleep = new GoalSleep(this, Time.TICKS_PER_HOUR * 4, Time.TICKS_PER_HOUR);
        this.goalSelector.addGoal(0, new GoalSwim(this));
        this.goalSelector.addGoal(1, new GoalPanic(this, 2.0D));
        this.goalSelector.addGoal(2, this.goalSleep);
//        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
//        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25D, false));
//        this.goalSelector.addGoal(5, new FollowMotherGoal(this, 1.25D));
//        this.goalSelector.addGoal(6, this.eatGrassGoal);
        this.goalSelector.addGoal(7, new GoalWaterAvoidingRandomWalking(this, 1.0D));
        this.goalSelector.addGoal(8, new GoalLookRandomly(this));
    }

    @Override
    public int skeletonTime() {
        return 14 * Time.TICKS_PER_DAY;
    }

    @Override
    public void spawnBaby() {
        //TODO implementation

    }

    public float tailIncX() {
        if (this.tailTimerX-- > 0) {
            this.tailIncX += Mth.PI / 200.0F;
        }
        else if (this.getTailChanceX()) {
            this.tailTimerX = 200;
            this.tailIncX = 0.0F;
        }
        else {
            this.tailIncX = 0.0F;
        }
        return this.tailIncX;
    }

    public float tailIncZ() {
        if (this.tailTimerZ-- > 0) {
            this.tailIncZ += Mth.PI * 2 / 500.0F;
        }
        else if (this.getTailChanceZ()) {
            this.tailTimerZ = 500;
            this.tailIncZ = 0.0F;
        }
        else {
            this.tailIncZ = 0.0F;
        }
        return this.tailIncZ;
    }
}