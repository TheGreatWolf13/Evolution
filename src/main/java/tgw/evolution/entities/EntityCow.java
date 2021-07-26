package tgw.evolution.entities;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.entities.ai.*;
import tgw.evolution.util.EntityStates;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.Time;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

public class EntityCow extends EntityGenericAnimal<EntityCow> {

    private int eatTimer;
    //    private EatGrassGoal eatGrassGoal;
    private SleepGoal sleepGoal;
    private float tailIncX;
    private float tailIncZ;
    private int tailTimerX;
    private int tailTimerZ;

    public EntityCow(EntityType<EntityCow> type, World worldIn) {
        super(type, worldIn);
        super.setAge(this.getAdultAge());
        if (!worldIn.isRemote) {
            this.sleepGoal.setSleepTimer();
        }
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
        return 4 * Time.YEAR_IN_TICKS;
    }

    @Override
    public int getAdultAge() {
        return 2 * Time.YEAR_IN_TICKS;
    }

    @Override
    public float getBaseHealth() {
        //TODO implementation
        return 10;
    }

    @Override
    public float getBaseWalkForce() {
        //TODO implementation
        return 0.025f;
    }

    @Override
    public float getFrictionModifier() {
        return 2.0f;
    }

    @Override
    public int getGestationPeriod() {
        return 9 * Time.MONTH_IN_TICKS;
    }

    @OnlyIn(Dist.CLIENT)
    public float getHeadRotationAngleX(float partialTicks) {
        if (this.hurtTime > 0) {
            this.eatTimer = 0;
            return 0.0F;
        }
        if (this.eatTimer > 4 && this.eatTimer <= 36) {
            float f = (this.eatTimer - 4 - partialTicks) / 32.0F;
            return MathHelper.PI / 5.0F + 0.219_911_49F * MathHelper.sin(f * 28.7F);
        }
        return this.eatTimer > 0 ? MathHelper.PI / 5.0F : MathHelper.degToRad(this.rotationPitch);
    }

    @OnlyIn(Dist.CLIENT)
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

    @Nullable
    @Override
    public HitboxEntity<EntityCow> getHitbox() {
        return null;
    }

    @Override
    public double getLegHeight() {
        //TODO implementation
        return 0.8;
    }

    @Override
    public double getLegSlowDown() {
        //TODO implementation
        return 0.1;
    }

    @Override
    public double getMass() {
        //TODO implementation
        return 700;
    }

    @Override
    public int getNumberOfBabies() {
        return this.rand.nextInt(100) == 0 ? 2 : 1;
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        if (poseIn == Pose.DYING) {
            return EntitySize.flexible(1.2F, 0.7F).scale(this.getRenderScale());
        }
        if (poseIn == Pose.SLEEPING) {
            return EntitySize.flexible(0.9F, 0.7F).scale(this.getRenderScale());
        }
        return EntitySize.flexible(0.9F, 1.4F).scale(this.getRenderScale());
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
        if (poseIn == Pose.DYING) {
            return 0.3F;
        }
        if (poseIn == Pose.SLEEPING) {
            return 0.85F;
        }
        return this.isChild() ? sizeIn.height * 0.95F : 1.35F;
    }

    @OnlyIn(Dist.CLIENT)
    private boolean getTailChanceX() {
        if (this.isDead() || this.isSleeping()) {
            return false;
        }
        return this.rand.nextInt(10_000) == 0;
    }

    @OnlyIn(Dist.CLIENT)
    private boolean getTailChanceZ() {
        if (this.isDead() || this.isSleeping()) {
            return false;
        }
        return this.rand.nextInt(5_000) == 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == EntityStates.EAT_GRASS) {
            this.eatTimer = 40;
        }
        else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public boolean hasHitboxes() {
        //TODO implementation
        return false;
    }

    @Override
    public void livingTick() {
        if (this.world.isRemote) {
            this.eatTimer = Math.max(0, this.eatTimer - 1);
        }
        if (!this.world.isRemote && this.world.getDayTime() % 24_000 == 0) {
            this.sleepGoal.setSleepTimer();
        }
        super.livingTick();
    }

    @Override
    public float mortallyRate() {
        //TODO implementation
        return 0;
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.sleepGoal.setSleepTime(this.sleepTime);
    }

    @Override
    protected void registerGoals() {
//        this.eatGrassGoal = new EatGrassGoal(this);
        this.sleepGoal = new SleepGoal(this, Time.HOUR_IN_TICKS * 4, Time.HOUR_IN_TICKS);
        this.goalSelector.addGoal(0, new SwinGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, this.sleepGoal);
//        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
//        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25D, false));
//        this.goalSelector.addGoal(5, new FollowMotherGoal(this, 1.25D));
//        this.goalSelector.addGoal(6, this.eatGrassGoal);
        this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
    }

    @Override
    public int skeletonTime() {
        return 14 * Time.DAY_IN_TICKS;
    }

    @Override
    public void spawnBaby() {
        //TODO implementation

    }

    @OnlyIn(Dist.CLIENT)
    public float tailIncX() {
        if (this.tailTimerX-- > 0) {
            this.tailIncX += MathHelper.PI / 200.0F;
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

    @OnlyIn(Dist.CLIENT)
    public float tailIncZ() {
        if (this.tailTimerZ-- > 0) {
            this.tailIncZ += MathHelper.PI * 2 / 500.0F;
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

    @Override
    protected void updateAITasks() {
        if (!this.isDead()) {
//            this.eatTimer = this.eatGrassGoal.getEatingGrassTimer();
            this.sleepTime = this.sleepGoal.getSleepTimer();
        }
        super.updateAITasks();
    }
}