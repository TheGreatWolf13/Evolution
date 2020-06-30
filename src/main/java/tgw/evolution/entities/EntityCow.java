package tgw.evolution.entities;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.entities.ai.*;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.util.EnumFoodNutrients;
import tgw.evolution.util.Time;

public class EntityCow extends AnimalEntity {

    private int eatTimer;
    private int tailTimerX;
    private float tailIncX;
    private float tailIncZ;
    private int tailTimerZ;
    private EatGrassGoal eatGrassGoal;
    private SleepGoal sleepGoal;

    public EntityCow(EntityType<EntityCow> type, World worldIn) {
        super(type, worldIn);
        super.setLifeSpan(this.getLifeSpan());
        this.food.set(EnumFoodNutrients.FOOD, 100);
        super.setAge(this.getAdultAge());
        if (!worldIn.isRemote) {
            this.sleepGoal.setSleepTimer();
        }
    }

    @Override
    protected void registerGoals() {
        this.eatGrassGoal = new EatGrassGoal(this);
        this.sleepGoal = new SleepGoal(this, Time.HOUR_IN_TICKS * 4, Time.HOUR_IN_TICKS);
        this.goalSelector.addGoal(0, new SwinGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, this.sleepGoal);
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new FollowMotherGoal(this, 1.25D));
        this.goalSelector.addGoal(6, this.eatGrassGoal);
        this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
    }

    @Override
    protected void updateAITasks() {
        if (!this.isDead()) {
            this.eatTimer = this.eatGrassGoal.getEatingGrassTimer();
            this.sleepTime = this.sleepGoal.getSleepTimer();
        }
        super.updateAITasks();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 10) {
            this.eatTimer = 40;
        }
        else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public void livingTick() {
        if (this.world.isRemote) {
            this.eatTimer = Math.max(0, this.eatTimer - 1);
        }
        if (!this.world.isRemote && this.world.getDayTime() % 24000 == 0) {
            this.sleepGoal.setSleepTimer();
        }
        super.livingTick();
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2F);
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
    public float getHeadRotationPointY(float partialTicks) {
        if (this.hurtTime > 0) {
            this.eatTimer = 0;
            return 0F;
        }
        if (this.eatTimer <= 0) {
            return 0.0F;
        }
        if (this.eatTimer >= 4 && this.eatTimer <= 36) {
            return 1.0F;
        }
        return this.eatTimer < 4 ? (this.eatTimer - partialTicks) / 4.0F : -(this.eatTimer - 40 - partialTicks) / 4.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public float getHeadRotationAngleX(float partialTicks) {
        if (this.hurtTime > 0) {
            this.eatTimer = 0;
            return 0F;
        }
        if (this.eatTimer > 4 && this.eatTimer <= 36) {
            float f = (this.eatTimer - 4 - partialTicks) / 32.0F;
            return (float) Math.PI / 5F + 0.21991149F * MathHelper.sin(f * 28.7F);
        }
        return this.eatTimer > 0 ? (float) Math.PI / 5F : this.rotationPitch * ((float) Math.PI / 180F);
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

    @OnlyIn(Dist.CLIENT)
    private boolean getTailChanceX() {
        if (this.isDead() || this.isSleeping()) {
            return false;
        }
        return this.rand.nextInt(10000) == 0;
    }

    @OnlyIn(Dist.CLIENT)
    private boolean getTailChanceZ() {
        if (this.isDead() || this.isSleeping()) {
            return false;
        }
        return this.rand.nextInt(5000) == 0;
    }

    @OnlyIn(Dist.CLIENT)
    public float tailIncX() {
        if (this.tailTimerX-- > 0) {
            this.tailIncX += (float) Math.PI / 200F;
        }
        else if (this.getTailChanceX()) {
            this.tailTimerX = 200;
            this.tailIncX = 0F;
        }
        else {
            this.tailIncX = 0F;
        }
        return this.tailIncX;
    }

    @OnlyIn(Dist.CLIENT)
    public float tailIncZ() {
        if (this.tailTimerZ-- > 0) {
            this.tailIncZ += (float) Math.PI * 2 / 500F;
        }
        else if (this.getTailChanceZ()) {
            this.tailTimerZ = 500;
            this.tailIncZ = 0F;
        }
        else {
            this.tailIncZ = 0F;
        }
        return this.tailIncZ;
    }

    @Override
    public Class<? extends AnimalEntity> getPartnerClass() {
        return EntityBull.class;
    }

    @Override
    public Class<? extends AnimalEntity> getFemaleClass() {
        return EntityCow.class;
    }

    @Override
    public AgeableEntity createChild(AgeableEntity ageable) {
        return EvolutionEntities.COW.get().create(this.world);
    }

    @Override
    public int getGestationTime() {
        return (int) (Time.MONTH_IN_TICKS * 9.5);
    }

    @Override
    public int getLifeSpan() {
        return Time.YEAR_IN_TICKS * 5 + this.rand.nextInt(Time.YEAR_IN_TICKS * 5);
    }

    @Override
    public int getAdultAge() {
        return Time.YEAR_IN_TICKS * 2;
    }

    @Override
    public int getOldAge() {
        return this.getDeterminedLifeSpan() - Time.YEAR_IN_TICKS;
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.sleepGoal.setSleepTime(this.sleepTime);
    }

    @Override
    public int skeletonTime() {
        return 14 * Time.DAY_IN_TICKS;
    }
}