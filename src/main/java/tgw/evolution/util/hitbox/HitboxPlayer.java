package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.HMDummy;
import tgw.evolution.util.hitbox.hms.HMPlayer;
import tgw.evolution.util.hitbox.hrs.HRPlayer;

public final class HitboxPlayer extends HitboxEntity<Player> implements HMPlayer<Player>, HRPlayer<Player>, IHitboxArmed<Player> {

    private final HitboxGroup armL;
    private final HitboxGroup armR;
    private final HitboxGroup body;
    private final HitboxGroup forearmL;
    private final HitboxGroup forearmR;
    private final HitboxGroup forelegL;
    private final HitboxGroup forelegR;
    private final Hitbox handL;
    private final Hitbox handR;
    private final Hitbox head;
    private final HitboxAttachable itemL;
    private final HitboxAttachable itemR;
    private final HitboxGroup legL;
    private final HitboxGroup legR;
    private final boolean slim;
    private boolean crouching;
    private ArmPose leftArmPose = ArmPose.EMPTY;
    private ArmPose rightArmPose = ArmPose.EMPTY;
    private boolean shouldCancelLeft;
    private boolean shouldCancelRight;
    private float swimAmount;

    public HitboxPlayer(boolean slim) {
        this.slim = slim;
        //Head
        this.head = this.addBox(HitboxType.HEAD, HitboxLib.HUMANOID_HEAD, 0, 24, 0, this);
        //Body
        this.body = new HitboxGroup(this, 0, 24, 0);
        this.addBox(HitboxType.CHEST, HitboxLib.HUMANOID_CHEST, this.body);
        //      Left arm
        this.armL = new HitboxGroup(this.body, -5, -2, 0);
        this.addBox(HitboxType.SHOULDER_LEFT, slim ? box(-2, -2, -2, 3, 3, 4) : HitboxLib.HUMANOID_LEFT_SHOULDER, 0, 1, 0, this.armL);
        this.addBox(HitboxType.ARM_LEFT, slim ? box(-2, -6, -2, 3, 3, 4) : HitboxLib.HUMANOID_LEFT_ARM, 0, 2, 0, this.armL);
        //              Left Forearm
        this.forearmL = new HitboxGroup(this.armL, slim ? 0 : -1, -4, 0);
        this.addBox(HitboxType.ARM_LEFT, slim ? box(-2, -3, -2, 3, 3, 4) : HitboxLib.HUMANOID_LEFT_FOREARM, this.forearmL);
        this.handL = this.addBox(HitboxType.HAND_LEFT, slim ? box(-2, -6, -2, 3, 3, 4) : HitboxLib.HUMANOID_LEFT_HAND, 0, 0, 0, this.forearmL);
        this.itemL = this.addBoxAttachable(HitboxType.NONE, HitboxLib.ZERO, 0, 0, 0, 0, 0, 0, this.forearmL);
        //      Right arm
        this.armR = new HitboxGroup(this.body, 5, -2, 0);
        this.addBox(HitboxType.SHOULDER_RIGHT, slim ? box(-1, -2, -2, 3, 3, 4) : HitboxLib.HUMANOID_RIGHT_SHOULDER, 0, 1, 0, this.armR);
        this.addBox(HitboxType.ARM_RIGHT, slim ? box(-1, -6, -2, 3, 3, 4) : HitboxLib.HUMANOID_RIGHT_ARM, 0, 2, 0, this.armR);
        //              Right Forearm
        this.forearmR = new HitboxGroup(this.armR, slim ? 0 : 1, -4, 0);
        this.addBox(HitboxType.ARM_RIGHT, slim ? box(-1, -3, -2, 3, 3, 4) : HitboxLib.HUMANOID_RIGHT_FOREARM, this.forearmR);
        this.handR = this.addBox(HitboxType.HAND_RIGHT, slim ? box(-1, -6, -2, 3, 3, 4) : HitboxLib.HUMANOID_RIGHT_HAND, 0, 0, 0, this.forearmR);
        this.itemR = this.addBoxAttachable(HitboxType.NONE, HitboxLib.ZERO, 0, 0, 0, 0, 0, 0, this.forearmR);
        //      Left leg
        this.legL = new HitboxGroup(this.body, -1.9f, -12, 0);
        this.addBox(HitboxType.LEG_LEFT, HitboxLib.HUMANOID_LEG, this.legL);
        //              Left foreleg
        this.forelegL = new HitboxGroup(this.legL, 0, -6, 0);
        this.addBox(HitboxType.LEG_LEFT, HitboxLib.HUMANOID_FORELEG, this.forelegL);
        this.addBox(HitboxType.FOOT_LEFT, HitboxLib.HUMANOID_FOOT, this.forelegL);
        //      Right leg
        this.legR = new HitboxGroup(this.body, 1.9f, -12, 0);
        this.addBox(HitboxType.LEG_RIGHT, HitboxLib.HUMANOID_LEG, this.legR);
        //              Right foreleg
        this.forelegR = new HitboxGroup(this.legR, 0, -6, 0);
        this.addBox(HitboxType.LEG_LEFT, HitboxLib.HUMANOID_FORELEG, this.forelegR);
        this.addBox(HitboxType.FOOT_RIGHT, HitboxLib.HUMANOID_FOOT, this.forelegR);
        //
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
    public float attackTime() {
        return this.attackTime;
    }

    @Override
    public HM body() {
        return this.body;
    }

    @Override
    public HM cape() {
        return HMDummy.DUMMY;
    }

    @Override
    protected @Nullable Hitbox childGetEquipFor(Player entity, IMelee.IAttackType type, HumanoidArm arm) {
        if (type == IMelee.BARE_HAND_ATTACK) {
            if (!entity.getOffhandItem().isEmpty()) {
                return this.getHand(arm);
            }
            if (((ILivingEntityPatch) entity).getFollowUp() % 2 == 0) {
                return this.getHand(arm);
            }
            return this.getHand(arm.getOpposite());
        }
        ColliderHitbox collider = type.getCollider(arm);
        if (collider == null) {
            return null;
        }
        return collider.attach(this.getItemAttach(arm));
    }

    @Override
    public void childInit(Player entity, float partialTicks) {
        this.modelProperties(entity);
        this.renderOrInit(entity, this, partialTicks);
    }

    @Override
    public HM clothesArmL() {
        return HMDummy.DUMMY;
    }

    @Override
    public HM clothesArmR() {
        return HMDummy.DUMMY;
    }

    @Override
    public HM clothesBody() {
        return HMDummy.DUMMY;
    }

    @Override
    public HM clothesForearmL() {
        return HMDummy.DUMMY;
    }

    @Override
    public HM clothesForearmR() {
        return HMDummy.DUMMY;
    }

    @Override
    public HM clothesForelegL() {
        return HMDummy.DUMMY;
    }

    @Override
    public HM clothesForelegR() {
        return HMDummy.DUMMY;
    }

    @Override
    public HM clothesLegL() {
        return HMDummy.DUMMY;
    }

    @Override
    public HM clothesLegR() {
        return HMDummy.DUMMY;
    }

    @Override
    public boolean crouching() {
        return this.crouching;
    }

    @Override
    public HM forearmL() {
        return this.forearmL;
    }

    @Override
    public HM forearmR() {
        return this.forearmR;
    }

    @Override
    public HM forelegL() {
        return this.forelegL;
    }

    @Override
    public HM forelegR() {
        return this.forelegR;
    }

    @Override
    public Hitbox getHand(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? this.handR : this.handL;
    }

    @Override
    public HitboxAttachable getItemAttach(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? this.itemR : this.itemL;
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
    public boolean isSlim() {
        return this.slim;
    }

    @Override
    public HM itemL() {
        return this.itemL;
    }

    @Override
    public HM itemR() {
        return this.itemR;
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
    public ArmPose rightArmPose() {
        return this.rightArmPose;
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
