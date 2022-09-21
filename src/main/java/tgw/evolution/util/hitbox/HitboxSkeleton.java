package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.HMDummy;
import tgw.evolution.util.hitbox.hms.HMSkeleton;
import tgw.evolution.util.hitbox.hrs.HRSkeleton;

public class HitboxSkeleton extends HitboxEntity<AbstractSkeleton>
        implements HMSkeleton<AbstractSkeleton>, HRSkeleton, IHitboxArmed<AbstractSkeleton> {

    private final HitboxGroup armL;
    private final HitboxGroup armR;
    private final Hitbox body = this.addBox(HitboxType.CHEST, HitboxLib.HUMANOID_CHEST, 0, 24, 0);
    private final HitboxAttachable handL;
    private final HitboxAttachable handR;
    private final Hitbox head = this.addBox(HitboxType.HEAD, HitboxLib.HUMANOID_HEAD, 0, 24, 0);
    private final HitboxGroup legL;
    private final HitboxGroup legR;
    private boolean crouching;
    private ArmPose leftArmPose = ArmPose.EMPTY;
    private ArmPose rightArmPose = ArmPose.EMPTY;
    private float swimAmount;

    public HitboxSkeleton() {
        //Left arm
        Hitbox shoulderL = this.addBox(HitboxType.SHOULDER_LEFT, HitboxLib.SKELETON_SHOULDER, -5, 22, 0);
        Hitbox armL = this.addBox(HitboxType.ARM_LEFT, HitboxLib.SKELETON_ARM, -5, 22, 0);
        this.handL = this.addBoxAttachable(HitboxType.HAND_LEFT, HitboxLib.SKELETON_HAND, -5, 22, 0, 0, -8, 0);
        this.armL = new HitboxGroup(shoulderL, armL, this.handL);
        //Right arm
        Hitbox shoulderR = this.addBox(HitboxType.SHOULDER_RIGHT, HitboxLib.SKELETON_SHOULDER, 5, 22, 0);
        Hitbox armR = this.addBox(HitboxType.ARM_RIGHT, HitboxLib.SKELETON_ARM, 5, 22, 0);
        this.handR = this.addBoxAttachable(HitboxType.HAND_RIGHT, HitboxLib.SKELETON_HAND, 5, 22, 0, 0, -8, 0);
        this.armR = new HitboxGroup(shoulderR, armR, this.handR);
        //Left leg
        Hitbox legL = this.addBox(HitboxType.LEG_LEFT, HitboxLib.SKELETON_LEG, -2, 12, 0);
        Hitbox footL = this.addBox(HitboxType.FOOT_LEFT, HitboxLib.SKELETON_FOOT, -2, 12, 0);
        this.legL = new HitboxGroup(legL, footL);
        //Right leg
        Hitbox legR = this.addBox(HitboxType.LEG_RIGHT, HitboxLib.SKELETON_LEG, 2, 12, 0);
        Hitbox footR = this.addBox(HitboxType.FOOT_RIGHT, HitboxLib.SKELETON_FOOT, 2, 12, 0);
        this.legR = new HitboxGroup(legR, footR);
        this.finish();
    }

    @Override
    public HM body() {
        return this.body;
    }

    @Override
    protected void childFinish() {
        this.armR.finish();
        this.armL.finish();
        this.legR.finish();
        this.legL.finish();
    }

    @Override
    protected @Nullable Hitbox childGetEquipFor(IMelee.@Nullable IAttackType type, HumanoidArm arm) {
        return null;
    }

    @Override
    public void childInit(AbstractSkeleton entity, float partialTicks) {
        this.renderOrInit(entity, this, partialTicks);
    }

    @Override
    public boolean crouching() {
        return this.crouching;
    }

    @Override
    public HitboxAttachable getArm(HumanoidArm arm) {
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
    public HM leftArm() {
        return this.armL;
    }

    @Override
    public ArmPose leftArmPose() {
        return this.leftArmPose;
    }

    @Override
    public HM leftLeg() {
        return this.legL;
    }

    @Override
    public HMSkeleton<AbstractSkeleton> model() {
        return this;
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
    public HM rightArm() {
        return this.armR;
    }

    @Override
    public ArmPose rightArmPose() {
        return this.rightArmPose;
    }

    @Override
    public HM rightLeg() {
        return this.legR;
    }

    @Override
    public void setAgeInTicks(float ageInTicks) {
        this.ageInTicks = ageInTicks;
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
    public void setRightArmPose(ArmPose rightArmPose) {
        this.rightArmPose = rightArmPose;
    }

    @Override
    public void setSwimAmount(float swimAmount) {
        this.swimAmount = swimAmount;
    }

    @Override
    public float swimAmount() {
        return this.swimAmount;
    }
}
