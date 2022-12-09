package tgw.evolution.util.hitbox;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.HMAbstractZombie;
import tgw.evolution.util.hitbox.hms.HMDummy;
import tgw.evolution.util.math.MathHelper;

public final class HitboxZombie extends HitboxEntity<Zombie> implements HMAbstractZombie<Zombie>, IHitboxArmed<Zombie> {

    private final Hitbox body;
    private final HitboxAttachable handL;
    private final HitboxAttachable handR;
    private final Hitbox head;
    private final HitboxGroup leftArm;
    private final HitboxGroup leftLeg;
    private final HitboxGroup rightArm;
    private final HitboxGroup rightLeg;
    private float attackTime;
    private boolean crouching;
    private ArmPose leftArmPose;
    private float limbSwing;
    private float limbSwingAmount;
    private float remainingItemUseTime;
    private boolean riding;
    private ArmPose rightArmPose;
    private boolean shouldCancelLeft;
    private boolean shouldCancelRight;
    private float swimAmount;
    private boolean young;

    public HitboxZombie() {
        //Head
        this.head = this.addBox(HitboxType.HEAD, HitboxLib.HUMANOID_HEAD, this);
        //Body
        this.body = this.addBox(HitboxType.CHEST, HitboxLib.HUMANOID_CHEST, this);
        //Left arm
        this.leftArm = new HitboxGroup(this);
        Hitbox shoulderL = this.addBox(HitboxType.SHOULDER_LEFT, HitboxLib.HUMANOID_LEFT_SHOULDER, this.leftArm);
        Hitbox armL = this.addBox(HitboxType.ARM_LEFT, HitboxLib.HUMANOID_LEFT_ARM, this.leftArm);
        this.handL = this.addBoxAttachable(HitboxType.HAND_LEFT, HitboxLib.HUMANOID_LEFT_HAND, -5, 22, 0, 0, -8, 0, this.leftArm);
        //Right arm
        this.rightArm = new HitboxGroup(this);
        Hitbox shoulderR = this.addBox(HitboxType.SHOULDER_RIGHT, HitboxLib.HUMANOID_RIGHT_SHOULDER, this.rightArm);
        Hitbox armR = this.addBox(HitboxType.ARM_RIGHT, HitboxLib.HUMANOID_RIGHT_ARM, this.rightArm);
        this.handR = this.addBoxAttachable(HitboxType.HAND_RIGHT, HitboxLib.HUMANOID_RIGHT_HAND, 5, 22, 0, 0, -8, 0, this.rightArm);
        //Left leg
        this.leftLeg = new HitboxGroup(this);
        Hitbox legL = this.addBox(HitboxType.LEG_LEFT, HitboxLib.HUMANOID_LEG, this.leftLeg);
        Hitbox footL = this.addBox(HitboxType.FOOT_LEFT, HitboxLib.HUMANOID_FOOT, this.leftLeg);
        //Right leg
        this.rightLeg = new HitboxGroup(this);
        Hitbox legR = this.addBox(HitboxType.LEG_RIGHT, HitboxLib.HUMANOID_LEG, this.rightLeg);
        Hitbox footR = this.addBox(HitboxType.FOOT_RIGHT, HitboxLib.HUMANOID_FOOT, this.rightLeg);
        this.finish();
    }

    @Override
    public boolean aggresive(Zombie entity) {
        return entity.isAggressive();
    }

    @Override
    public HM armL() {
        return this.leftArm;
    }

    @Override
    public HM armR() {
        return this.rightArm;
    }

    @Override
    public float attackTime() {
        return this.attackTime;
    }

    @Override
    public HM body() {
        return this.body;
    }

    @Override
    protected @Nullable Hitbox childGetEquipFor(Zombie entity, IMelee.@Nullable IAttackType type, HumanoidArm arm) {
        return null;
    }

    @Override
    public void childInit(Zombie entity, float partialTicks) {
        this.limbSwing = MathHelper.getLimbSwing(entity, partialTicks);
        this.limbSwingAmount = MathHelper.getLimbSwingAmount(entity, partialTicks);
        this.swimAmount = entity.getSwimAmount(partialTicks);
        this.remainingItemUseTime = entity.getUseItemRemainingTicks();
        this.riding = MathHelper.isSitting(entity);
//        ItemStack mainhandStack = entity.getMainHandItem();
//        ItemStack offhandStack = entity.getOffhandItem();
        this.rightArmPose = ArmPose.getArmPose(entity, InteractionHand.MAIN_HAND);
        this.leftArmPose = ArmPose.getArmPose(entity, InteractionHand.OFF_HAND);
        this.attackTime = MathHelper.getAttackAnim(entity, partialTicks);
        this.crouching = entity.getPose() == Pose.CROUCHING;
        this.ageInTicks = MathHelper.getAgeInTicks(entity, partialTicks);
        //Main
        Pose pose = entity.getPose();
        switch (pose) {
            case CROUCHING, STANDING -> {
                this.pivotX = 0;
                this.pivotZ = 0;
            }
            case SLEEPING -> {
                Direction direction = entity.getBedOrientation();
                if (direction == null) {
                    direction = Direction.WEST;
                }
                this.setPivot(-26 * direction.getStepX(), 0, -26 * direction.getStepZ(), 1.0f / 16);
            }
        }
        if (this.swimAmount > 0) {
            if (!entity.isInWater()) {
                float waterInclination = -90.0F;
                float waterPitch = Mth.lerp(this.swimAmount, 0.0F, waterInclination);
                float sinWaterPitch = MathHelper.sinDeg(waterPitch);
                float cosWaterPitch = MathHelper.cosDeg(waterPitch);
            }
        }
        //Mess
        float viewYaw = entity.getViewYRot(partialTicks);
    }

    @Override
    public boolean crouching() {
        return this.crouching;
    }

    @Override
    public HM forearmL() {
        //TODO implementation
        return HMDummy.DUMMY;
    }

    @Override
    public HM forearmR() {
        //TODO implementation
        return HMDummy.DUMMY;
    }

    @Override
    public HM forelegL() {
        //TODO implementation
        return HMDummy.DUMMY;
    }

    @Override
    public HM forelegR() {
        //TODO implementation
        return HMDummy.DUMMY;
    }

    @Override
    public Hitbox getHand(HumanoidArm arm) {
        //TODO implementation
        return null;
    }

    @Override
    public HitboxAttachable getItemAttach(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? this.handR : this.handL;
    }

    @Override
    public HM hat() {
        return HMDummy.DUMMY;
    }

    @Override
    public HM head() {
        return this.head;
    }

    @Override
    protected Hitbox headOrRoot() {
        return this.head;
    }

    @Override
    public HM itemL() {
        //TODO implementation
        return null;
    }

    @Override
    public HM itemR() {
        //TODO implementation
        return null;
    }

    @Override
    public ArmPose leftArmPose() {
        return this.leftArmPose;
    }

    @Override
    public HM legL() {
        return this.leftLeg;
    }

    @Override
    public HM legR() {
        return this.rightLeg;
    }

    @Override
    protected double relativeHeadOrRootX() {
        return 0;
    }

    @Override
    protected double relativeHeadOrRootY() {
        return 4 / 16.0;
    }

    @Override
    protected double relativeHeadOrRootZ() {
        return -4 / 16.0;
    }

    @Override
    public boolean riding() {
        return this.riding;
    }

    @Override
    public ArmPose rightArmPose() {
        return this.rightArmPose;
    }

    @Override
    public void setAttackTime(float attackTime) {
        this.attackTime = attackTime;
    }

    @Override
    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    @Override
    public void setLeftArmPose(ArmPose leftArmPose) {
        this.leftArmPose = leftArmPose;
    }

    @Override
    public void setRiding(boolean riding) {
        this.riding = riding;
    }

    @Override
    public void setRightArmPose(ArmPose rightArmPose) {
        this.rightArmPose = rightArmPose;
    }

    @Override
    public void setShouldCancelLeft(boolean shouldCancel) {
        this.shouldCancelLeft = shouldCancel;
    }

    @Override
    public void setShouldCancelRight(boolean shouldCancel) {
        this.shouldCancelRight = shouldCancel;
    }

    @Override
    public void setSwimAmount(float swimAmount) {
        this.swimAmount = swimAmount;
    }

    @Override
    public void setYoung(boolean young) {
        this.young = young;
    }

    @Override
    public boolean shouldCancelLeft() {
        return this.shouldCancelLeft;
    }

    @Override
    public boolean shouldCancelRight() {
        return this.shouldCancelRight;
    }

    @Override
    public float swimAmount() {
        return this.swimAmount;
    }

    @Override
    public boolean young() {
        return this.young;
    }
}
