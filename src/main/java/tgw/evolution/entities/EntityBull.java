//package tgw.evolution.entities;
//
//import net.minecraft.entity.EntitySize;
//import net.minecraft.entity.EntityType;
//import net.minecraft.entity.Pose;
//import net.minecraft.entity.SharedMonsterAttributes;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.world.World;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import tgw.evolution.entities.ai.*;
//import tgw.evolution.util.EnumFoodNutrients;
//import tgw.evolution.util.time.Time;
//
//public class EntityBull extends AnimalEntity {
//
//    private int eatTimer;
//    private int tailTimerX;
//    private float tailIncX;
//    private float tailIncZ;
//    private int tailTimerZ;
//    private EatGrassGoal eatGrassGoal;
//
//    public EntityBull(EntityType<EntityBull> type, World worldIn) {
//        super(type, worldIn);
//        super.setLifeSpan(this.getLifeSpan());
//        this.food.set(EnumFoodNutrients.FOOD, 100);
//        super.setAge(this.getAdultAge());
//        this.pregnancyTime = -Time.MONTH_IN_TICKS;
//    }
//
//    @Override
//    public int getLifeSpan() {
//        return 5 * Time.YEAR_IN_TICKS + this.rand.nextInt(10 * Time.YEAR_IN_TICKS);
//    }
//
//    @Override
//    public int getAdultAge() {
//        return 2 * Time.YEAR_IN_TICKS;
//    }
//
//    @Override
//    public int getOldAge() {
//        return this.getDeterminedLifeSpan() - Time.YEAR_IN_TICKS;
//    }
//
//    @Override
//    protected void registerGoals() {
//        this.eatGrassGoal = new EatGrassGoal(this);
//        this.goalSelector.addGoal(0, new GoalSwim(this));
//        this.goalSelector.addGoal(1, new GoalPanic(this, 2.0D));
//        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
//        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, false));
//        this.goalSelector.addGoal(4, new FollowMotherGoal(this, 1.25D));
//        this.goalSelector.addGoal(5, this.eatGrassGoal);
//        this.goalSelector.addGoal(6, new GoalWaterAvoidingRandomWalking(this, 1.0D));
//        this.goalSelector.addGoal(7, new GoalLookRandomly(this));
//    }
//
//    @Override
//    protected void registerAttributes() {
//        super.registerAttributes();
//        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
//        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2F);
//    }
//
//    @Override
//    public int skeletonTime() {
//        return 14 * Time.DAY_IN_TICKS;
//    }
//
//    @Override
//    protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
//        return this.isChild() ? sizeIn.height * 0.95F : 1.35F;
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public float getHeadRotationPointY(float partialTicks) {
//        if (this.hurtTime > 0) {
//            this.eatTimer = 0;
//            return 0F;
//        }
//        if (this.eatTimer <= 0) {
//            return 0.0F;
//        }
//        if (this.eatTimer >= 4 && this.eatTimer <= 36) {
//            return 1.0F;
//        }
//        return this.eatTimer < 4 ? (this.eatTimer - partialTicks) / 4.0F : -(this.eatTimer - 40 - partialTicks) / 4.0F;
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public float getHeadRotationAngleX(float partialTicks) {
//        if (this.hurtTime > 0) {
//            this.eatTimer = 0;
//            return 0F;
//        }
//        if (this.eatTimer > 4 && this.eatTimer <= 36) {
//            float f = (this.eatTimer - 4 - partialTicks) / 32.0F;
//            return (float) Math.PI / 5F + 0.21991149F * MathHelper.sin(f * 28.7F);
//        }
//        return this.eatTimer > 0 ? (float) Math.PI / 5F : this.rotationPitch * ((float) Math.PI / 180F);
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public float tailIncX() {
//        if (this.tailTimerX-- > 0) {
//            this.tailIncX += (float) Math.PI / 200F;
//        }
//        else if (this.getTailChanceX()) {
//            this.tailTimerX = 200;
//            this.tailIncX = 0F;
//        }
//        else {
//            this.tailIncX = 0F;
//        }
//        return this.tailIncX;
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    private boolean getTailChanceX() {
//        if (this.isDead()) {
//            return false;
//        }
//        return this.rand.nextInt(10000) == 0;
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public float tailIncZ() {
//        if (this.tailTimerZ-- > 0) {
//            this.tailIncZ += (float) Math.PI * 2 / 500F;
//        }
//        else if (this.getTailChanceZ()) {
//            this.tailTimerZ = 500;
//            this.tailIncZ = 0F;
//        }
//        else {
//            this.tailIncZ = 0F;
//        }
//        return this.tailIncZ;
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    private boolean getTailChanceZ() {
//        if (this.isDead()) {
//            return false;
//        }
//        return this.rand.nextInt(5000) == 0;
//    }
//
//    @Override
//    public int getGestationPeriod() {
//        return 0;
//    }
//
//    @Override
//    public void livingTick() {
//        if (this.world.isRemote) {
//            this.eatTimer = Math.max(0, this.eatTimer - 1);
//        }
//        super.livingTick();
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    @Override
//    public void handleStatusUpdate(byte id) {
//        if (id == 10) {
//            this.eatTimer = 40;
//        }
//        else {
//            super.handleStatusUpdate(id);
//        }
//    }
//
//    @Override
//    protected void updateAITasks() {
//        this.eatTimer = this.eatGrassGoal.getEatingGrassTimer();
//        super.updateAITasks();
//    }
//
//    @Override
//    public Class<? extends AnimalEntity> getPartnerClass() {
//        return EntityCow.class;
//    }
//
//    @Override
//    public Class<? extends AnimalEntity> getFemaleClass() {
//        return EntityCow.class;
//    }
//
//    @Override
//    public AgeableEntity createChild(AgeableEntity ageable) {
//        return null;
//    }
//}
