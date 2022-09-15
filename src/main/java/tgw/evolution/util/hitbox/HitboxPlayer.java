package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.HMDummy;
import tgw.evolution.util.hitbox.hms.HMPlayer;
import tgw.evolution.util.hitbox.hrs.HRPlayer;

public final class HitboxPlayer extends HitboxEntity<Player> implements HMPlayer<Player>, HRPlayer, IHitboxArmed<Player> {

    private final HitboxGroup armL;
    private final HitboxGroup armR;
    private final Hitbox body = this.addBox(HitboxType.CHEST, HitboxLib.HUMANOID_CHEST, 0, 24, 0);
    private final HitboxAttachable handL;
    private final HitboxAttachable handR;
    private final Hitbox head = this.addBox(HitboxType.HEAD, HitboxLib.HUMANOID_HEAD, 0, 24, 0);
    private final HitboxGroup legL;
    private final HitboxGroup legR;
    private float attackTime;
    private boolean crouching;
    private ArmPose leftArmPose = ArmPose.EMPTY;
    private ArmPose rightArmPose = ArmPose.EMPTY;
    private float swimAmount;

    public HitboxPlayer(boolean slim) {
        Hitbox shoulderL = this.addBox(HitboxType.SHOULDER_LEFT, slim ? box(-2, -2, -2, 3, 4, 4) : HitboxLib.HUMANOID_LEFT_SHOULDER);
        Hitbox armL = this.addBox(HitboxType.ARM_LEFT, slim ? box(-2, -6, -2, 3, 4, 4) : HitboxLib.HUMANOID_LEFT_ARM);
        this.handL = this.addBoxAttachable(HitboxType.HAND_LEFT, slim ? box(-2, -10, -2, 3, 4, 4) : HitboxLib.HUMANOID_LEFT_HAND, 0, 0, 0, -1, -8, 0);
        Hitbox shoulderR = this.addBox(HitboxType.SHOULDER_RIGHT, slim ? box(-1, -2, -2, 3, 4, 4) : HitboxLib.HUMANOID_RIGHT_SHOULDER);
        Hitbox armR = this.addBox(HitboxType.ARM_RIGHT, slim ? box(-1, -6, -2, 3, 4, 4) : HitboxLib.HUMANOID_RIGHT_ARM);
        this.handR = this.addBoxAttachable(HitboxType.HAND_RIGHT, slim ? box(-1, -10, -2, 3, 4, 4) : HitboxLib.HUMANOID_RIGHT_HAND, 0, 0, 0, 1, -8,
                                           0);
        this.armL = new HitboxGroup(shoulderL, armL, this.handL);
        Hitbox legL = this.addBox(HitboxType.LEG_LEFT, HitboxLib.HUMANOID_LEG);
        Hitbox footL = this.addBox(HitboxType.FOOT_LEFT, HitboxLib.HUMANOID_FOOT);
        this.legL = new HitboxGroup(legL, footL);
        this.armR = new HitboxGroup(shoulderR, armR, this.handR);
        Hitbox legR = this.addBox(HitboxType.LEG_RIGHT, HitboxLib.HUMANOID_LEG);
        Hitbox footR = this.addBox(HitboxType.FOOT_RIGHT, HitboxLib.HUMANOID_FOOT);
        this.legR = new HitboxGroup(legR, footR);
        this.finish();
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
        this.armL.finish();
        this.legL.finish();
        this.armR.finish();
        this.legR.finish();
    }

    @Override
    protected @Nullable Hitbox childGetEquipFor(IMelee.IAttackType type, HumanoidArm arm) {
        if (type == IMelee.BARE_HAND_ATTACK) {
            return this.getArm(arm);
        }
        ColliderHitbox collider = type.getCollider(arm);
        if (collider == null) {
            return null;
        }
        return collider.attach(this.getArm(arm));
    }

    @Override
    public void childInit(Player entity, float partialTicks) {
        this.modelProperties(entity);
        this.renderOrInit(entity, this, partialTicks);
    }

    @Override
    public HM cloak() {
        return HMDummy.DUMMY;
    }

    @Override
    public boolean crouching() {
        return this.crouching;
    }

    @Override
    public HM ear() {
        return HMDummy.DUMMY;
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
    public HM jacket() {
        return HMDummy.DUMMY;
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
    public HM leftPants() {
        return HMDummy.DUMMY;
    }

    @Override
    public HM leftSleeve() {
        return HMDummy.DUMMY;
    }

    @Override
    public HMPlayer<Player> model() {
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
    public boolean riding() {
        return this.riding;
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
    public HM rightPants() {
        return HMDummy.DUMMY;
    }

    @Override
    public HM rightSleeve() {
        return HMDummy.DUMMY;
    }

    @Override
    public void setAgeInTicks(float ageInTicks) {
        this.ageInTicks = ageInTicks;
    }

    @Override
    public void setAllVisible(boolean visible) {
        //Do nothing
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
