package tgw.evolution.util.hitbox;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.HMAbstractZombie;
import tgw.evolution.util.hitbox.hms.HMDummy;
import tgw.evolution.util.math.MathHelper;

public class HitboxZombie extends HitboxEntity<Zombie> implements HMAbstractZombie<Zombie> {

    public static final Vec3 NECK_STANDING = new Vec3(0, 24 / 16.0, 0);
    protected final Hitbox armL = this.addBox(HitboxType.ARM_LEFT, HitboxLib.HUMANOID_LEFT_ARM);
    protected final Hitbox armR = this.addBox(HitboxType.ARM_RIGHT, HitboxLib.HUMANOID_RIGHT_ARM);
    protected final Hitbox body = this.addBox(HitboxType.CHEST, HitboxLib.HUMANOID_CHEST);
    protected final Hitbox footL = this.addBox(HitboxType.FOOT_LEFT, HitboxLib.HUMANOID_FOOT);
    protected final Hitbox footR = this.addBox(HitboxType.FOOT_RIGHT, HitboxLib.HUMANOID_FOOT);
    protected final Hitbox handL = this.addBox(HitboxType.HAND_LEFT, HitboxLib.HUMANOID_LEFT_HAND);
    protected final Hitbox handR = this.addBox(HitboxType.HAND_RIGHT, HitboxLib.HUMANOID_RIGHT_HAND);
    protected final Hitbox head = this.addBox(HitboxType.HEAD, HitboxLib.HUMANOID_HEAD);
    protected final HitboxGroup leftArm;
    protected final HitboxGroup leftLeg;
    protected final Hitbox legL = this.addBox(HitboxType.LEG_LEFT, HitboxLib.HUMANOID_LEG);
    protected final Hitbox legR = this.addBox(HitboxType.LEG_RIGHT, HitboxLib.HUMANOID_LEG);
    protected final HitboxGroup rightArm;
    protected final HitboxGroup rightLeg;
    protected final Hitbox shoulderL = this.addBox(HitboxType.SHOULDER_LEFT, HitboxLib.HUMANOID_LEFT_SHOULDER);
    protected final Hitbox shoulderR = this.addBox(HitboxType.SHOULDER_RIGHT, HitboxLib.HUMANOID_RIGHT_SHOULDER);
    protected float attackTime;
    protected boolean crouching;
    protected ArmPose leftArmPose;
    protected float limbSwing;
    protected float limbSwingAmount;
    protected float remainingItemUseTime;
    protected boolean riding;
    protected ArmPose rightArmPose;
    protected float swimAmount;
    protected boolean young;

    public HitboxZombie() {
        this.leftArm = new HitboxGroup(this.shoulderL, this.armL, this.handL);
        this.leftLeg = new HitboxGroup(this.legL, this.footL);
        this.rightArm = new HitboxGroup(this.shoulderR, this.armR, this.handR);
        this.rightLeg = new HitboxGroup(this.legR, this.footR);
        this.finish();
    }

    @Override
    public boolean aggresive(Zombie entity) {
        return entity.isAggressive();
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
    protected void childFinish() {
        this.leftArm.finish();
        this.leftLeg.finish();
        this.rightArm.finish();
        this.rightLeg.finish();
    }

    @Override
    protected @Nullable Hitbox childGetEquipFor(IMelee.@Nullable IAttackType type, HumanoidArm arm) {
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
    public HM leftArm() {
        return this.leftArm;
    }

    @Override
    public ArmPose leftArmPose() {
        return this.leftArmPose;
    }

    @Override
    public HM leftLeg() {
        return this.leftLeg;
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
    public HM rightArm() {
        return this.rightArm;
    }

    @Override
    public ArmPose rightArmPose() {
        return this.rightArmPose;
    }

    @Override
    public HM rightLeg() {
        return this.rightLeg;
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
    public void setSwimAmount(float swimAmount) {
        this.swimAmount = swimAmount;
    }

    @Override
    public void setYoung(boolean young) {
        this.young = young;
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
