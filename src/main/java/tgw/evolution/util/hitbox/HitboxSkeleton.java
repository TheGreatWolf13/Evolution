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

public final class HitboxSkeleton extends HitboxEntity<AbstractSkeleton>
        implements HMSkeleton<AbstractSkeleton>, HRSkeleton, IHitboxArmed<AbstractSkeleton> {

    private final HitboxGroup armL;
    private final HitboxGroup armR;
    private final Hitbox body;
    private final HitboxAttachable handL;
    private final HitboxAttachable handR;
    private final Hitbox head;
    private final HitboxGroup legL;
    private final HitboxGroup legR;
    private boolean crouching;
    private ArmPose leftArmPose = ArmPose.EMPTY;
    private ArmPose rightArmPose = ArmPose.EMPTY;
    private boolean shouldCancelLeft;
    private boolean shouldCancelRight;
    private float swimAmount;

    public HitboxSkeleton() {
        //Head
        this.head = this.addBox(HitboxType.HEAD, HitboxLib.HUMANOID_HEAD, 0, 24, 0, this);
        //Body
        this.body = this.addBox(HitboxType.CHEST, HitboxLib.HUMANOID_CHEST, 0, 24, 0, this);
        //Left arm
        this.armL = new HitboxGroup(this);
        this.addBox(HitboxType.SHOULDER_LEFT, HitboxLib.SKELETON_SHOULDER, -5, 22, 0, this.armL);
        this.addBox(HitboxType.ARM_LEFT, HitboxLib.SKELETON_ARM, -5, 22, 0, this.armL);
        this.handL = this.addBoxAttachable(HitboxType.HAND_LEFT, HitboxLib.SKELETON_HAND, -5, 22, 0, 0, -8, 0, this.armL);
        //Right arm
        this.armR = new HitboxGroup(this);
        this.addBox(HitboxType.SHOULDER_RIGHT, HitboxLib.SKELETON_SHOULDER, 5, 22, 0, this.armR);
        this.addBox(HitboxType.ARM_RIGHT, HitboxLib.SKELETON_ARM, 5, 22, 0, this.armR);
        this.handR = this.addBoxAttachable(HitboxType.HAND_RIGHT, HitboxLib.SKELETON_HAND, 5, 22, 0, 0, -8, 0, this.armR);
        //Left leg
        this.legL = new HitboxGroup(this);
        this.addBox(HitboxType.LEG_LEFT, HitboxLib.SKELETON_LEG, -2, 12, 0, this.legL);
        this.addBox(HitboxType.FOOT_LEFT, HitboxLib.SKELETON_FOOT, -2, 12, 0, this.legL);
        //Right leg
        this.legR = new HitboxGroup(this);
        this.addBox(HitboxType.LEG_RIGHT, HitboxLib.SKELETON_LEG, 2, 12, 0, this.legR);
        this.addBox(HitboxType.FOOT_RIGHT, HitboxLib.SKELETON_FOOT, 2, 12, 0, this.legR);
        this.finish();
    }

    @Override
    public HM armL() {
        return this.armL;
    }

    @Override
    public HM armR() {
        return this.armR;
    }

    @Override
    public HM body() {
        return this.body;
    }

    @Override
    protected @Nullable Hitbox childGetEquipFor(AbstractSkeleton entity, IMelee.@Nullable IAttackType type, HumanoidArm arm) {
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
        return this.legL;
    }

    @Override
    public HM legR() {
        return this.legR;
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
    public ArmPose rightArmPose() {
        return this.rightArmPose;
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
}
