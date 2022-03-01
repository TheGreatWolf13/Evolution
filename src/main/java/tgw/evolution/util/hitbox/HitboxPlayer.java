package tgw.evolution.util.hitbox;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import tgw.evolution.items.ICustomAttack;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.MixinTempHelper;
import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.math.MathHelper;

public class HitboxPlayer extends HitboxEntity<Player> {

    protected static final float SCALE = 0.937_5f;
    protected final Hitbox armL;
    protected final Hitbox armR;
    protected final Hitbox body = this.addBox(HitboxType.CHEST, aabb(-4, -12, -2, 4, 0, 2, SCALE));
    protected final Hitbox footL = this.addBox(HitboxType.LEFT_FOOT, HitboxLib.PLAYER_FOOT);
    protected final Hitbox footR = this.addBox(HitboxType.RIGHT_FOOT, HitboxLib.PLAYER_FOOT);
    protected final Hitbox handL;
    protected final Hitbox handR;
    protected final Hitbox head = this.addBox(HitboxType.HEAD, aabb(-4, 0, -4, 4, 8, 4, SCALE));
    protected final HitboxGroup leftArm;
    protected final HitboxGroup leftLeg;
    protected final Hitbox legL = this.addBox(HitboxType.LEFT_LEG, HitboxLib.PLAYER_LEG);
    protected final Hitbox legR = this.addBox(HitboxType.RIGHT_LEG, HitboxLib.PLAYER_LEG);
    protected final Hitbox mainhandSword;
    protected final HitboxGroup rightArm;
    protected final HitboxGroup rightLeg;
    protected final Hitbox shoulderL;
    protected final Hitbox shoulderR;
    protected float attackTime;
    protected boolean isSitting;
    protected boolean isSneak;
    protected ArmPose leftArmPose;
    protected float limbSwing;
    protected float limbSwingAmount;
    protected float remainingItemUseTime;
    protected ArmPose rightArmPose;
    protected float swimAnimation;

    public HitboxPlayer(boolean slim) {
        this.armL = this.addBox(HitboxType.LEFT_ARM, aabb(-1, -6, -2, slim ? 2 : 3, -2, 2, SCALE));
        this.shoulderL = this.addBox(HitboxType.LEFT_SHOULDER, aabb(-1, -2, -2, slim ? 2 : 3, 2, 2, SCALE));
        this.handL = this.addBox(HitboxType.LEFT_HAND, aabb(-1, -10, -2, slim ? 2 : 3, -6, 2, SCALE));
        this.armR = this.addBox(HitboxType.RIGHT_ARM, aabb(slim ? -2 : -3, -6, -2, 1, -2, 2, SCALE));
        this.shoulderR = this.addBox(HitboxType.RIGHT_SHOULDER, aabb(slim ? -2 : -3, -2, -2, 1, 2, 2, SCALE));
        this.handR = this.addBox(HitboxType.RIGHT_HAND, aabb(slim ? -2 : -3, -10, -2, 1, -6, 2, SCALE));
        this.mainhandSword = this.addEquip(HitboxType.BLADE, aabb(-0.5, -1, 13, -1.5, 1, 23.5, SCALE));
        this.leftArm = new HitboxGroup(this.shoulderL, this.armL, this.handL);
        this.leftLeg = new HitboxGroup(this.legL, this.footL);
        this.rightArm = new HitboxGroup(this.shoulderR, this.armR, this.handR, this.mainhandSword);
        this.rightLeg = new HitboxGroup(this.legR, this.footR);
        this.rightArm.setStartingRotationForBox(3, -MathHelper.PI_OVER_2, 0, 0);
    }

    private static float quadraticArmUpdate(float f) {
        return -65.0F * f + f * f;
    }

    protected HitboxGroup getArmForSide(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }

    @Override
    public Hitbox getEquipmentFor(ICustomAttack.AttackType type, InteractionHand hand) {
        switch (hand) {
            case MAIN_HAND -> {
                switch (type) {
                    case SWORD -> {
                        return this.mainhandSword;
                    }
                }
                throw new UnregisteredFeatureException("No hitbox registered for " + type + " on " + hand);
            }
            case OFF_HAND -> throw new UnregisteredFeatureException("No hitbox registered for " + type + " on " + hand);
        }
        throw new IllegalStateException("Unknown hand: " + hand);
    }

    @Override
    public void init(Player entity, float partialTicks) {
        this.reset();
        this.rotationYaw = -MathHelper.getEntityBodyYaw(entity, partialTicks);
        if (entity.getVehicle() instanceof AbstractHorse) {
            this.rotationYaw = -MathHelper.getEntityBodyYaw(entity.getVehicle(), partialTicks);
        }
        this.rotationPitch = -entity.getViewXRot(partialTicks);
        this.limbSwing = MathHelper.getLimbSwing(entity, partialTicks);
        this.limbSwingAmount = MathHelper.getLimbSwingAmount(entity, partialTicks);
        this.swimAnimation = MathHelper.getSwimAnimation(entity, partialTicks);
        this.remainingItemUseTime = entity.getUseItemRemainingTicks();
        this.isSitting = MathHelper.isSitting(entity);
        ItemStack mainhandStack = entity.getMainHandItem();
        ItemStack offhandStack = entity.getOffhandItem();
        this.rightArmPose = MathHelper.getArmPose(entity, mainhandStack, offhandStack, InteractionHand.MAIN_HAND);
        this.leftArmPose = MathHelper.getArmPose(entity, mainhandStack, offhandStack, InteractionHand.OFF_HAND);
        this.attackTime = MathHelper.getAttackAnim(entity, partialTicks);
        this.isSneak = entity.getPose() == Pose.CROUCHING;
        this.ageInTicks = MathHelper.getAgeInTicks(entity, partialTicks);
        //Main
        this.rotationY = MathHelper.degToRad(this.rotationYaw);
        float sinYaw = MathHelper.sinDeg(this.rotationYaw);
        float cosYaw = MathHelper.cosDeg(this.rotationYaw);
        Pose pose = entity.getPose();
        switch (pose) {
            case CROUCHING, STANDING -> {
                this.pivotX = -1 / 16.0f * sinYaw * (1 - this.swimAnimation);
                this.pivotZ = -1 / 16.0f * cosYaw * (1 - this.swimAnimation);
            }
            case SLEEPING -> {
                Direction direction = entity.getBedOrientation();
                if (direction == null) {
                    direction = Direction.WEST;
                }
                this.rotationY = MathHelper.degToRad(MathHelper.getAngleByDirection(direction) + 90);
                this.rotationX = MathHelper.degToRad(90);
                this.setPivot(-26 * direction.getStepX(), 0, -26 * direction.getStepZ(), SCALE / 16);
            }
        }
        if (this.swimAnimation > 0) {
            if (!entity.isInWater()) {
                float waterInclination = -90.0F;
                float waterPitch = Mth.lerp(this.swimAnimation, 0.0F, waterInclination);
                this.rotationX += MathHelper.degToRad(waterPitch);
                float sinWaterPitch = MathHelper.sinDeg(waterPitch);
                float cosWaterPitch = MathHelper.cosDeg(waterPitch);
                if (entity.isVisuallySwimming()) {
                    this.pivotX = 24.5f / 16.0f * -sinYaw * -sinWaterPitch + 4.4f / 16.0f * cosWaterPitch * -sinYaw;
                    this.pivotY = 4.4f / 16.0f * -sinWaterPitch - 24.5f / 16.0f * cosWaterPitch;
                    this.pivotZ = 24.5f / 16.0f * -cosYaw * -sinWaterPitch - 4.4f / 16.0f * cosWaterPitch * cosYaw;
                }
                else {
                    this.pivotX = 21 / 16.0f * -sinYaw * -sinWaterPitch + 1 / 16.0f * cosWaterPitch * -sinYaw;
                    this.pivotY = 1 / 16.0f * -sinWaterPitch - 21 / 16.0f * cosWaterPitch;
                    this.pivotZ = 21 / 16.0f * -cosYaw * -sinWaterPitch - 1 / 16.0f * cosWaterPitch * cosYaw;
                }
            }
            else {
                float waterInclination = -90.0F + this.rotationPitch;
                float waterPitch = Mth.lerp(this.swimAnimation, 0.0F, waterInclination);
                this.rotationX += MathHelper.degToRad(waterPitch);
                float sinWaterPitch = MathHelper.sinDeg(waterPitch);
                float cosWaterPitch = MathHelper.cosDeg(waterPitch);
                if (entity.isVisuallySwimming()) {
                    this.pivotX = 22.375f / 16.0f * -sinYaw * -sinWaterPitch - 4 / 16.0f * cosWaterPitch * -sinYaw;
                    this.pivotY = 6.358_310_793f / 16.0f - 22.391_689_21f / 16.0f * cosWaterPitch + 3.983_310_793f / 16.0f * sinWaterPitch;
                    this.pivotZ = 22.375f / 16.0f * -cosYaw * -sinWaterPitch + 4 / 16.0f * cosWaterPitch * cosYaw;
                }
                else {
                    this.pivotX = 21 / 16.0f * -sinYaw * -sinWaterPitch + 1 / 16.0f * cosWaterPitch * -sinYaw;
                    this.pivotY = 1 / 16.0f * -sinWaterPitch - 21 / 16.0f * cosWaterPitch;
                    this.pivotZ = 21 / 16.0f * -cosYaw * -sinWaterPitch - 1 / 16.0f * cosWaterPitch * cosYaw;
                }
            }
        }
        //Head
        this.head.setPivot(0, 24, 0, SCALE / 16);
        //Body
        this.body.setPivot(0, 24, 0, SCALE / 16);
        //Arm
        this.rightArm.setPivot(-5, 22, 0, SCALE / 16);
        this.leftArm.setPivot(5, 22, 0, SCALE / 16);
        //Leg
        this.rightLeg.setPivot(-2, 12, 0, SCALE / 16);
        this.leftLeg.setPivot(2, 12, 0, SCALE / 16);
        //Mess
        boolean isElytraFlying = entity.getFallFlyingTicks() > 4;
        boolean isVisuallySwimming = entity.isVisuallySwimming();
        float viewYaw = entity.getViewYRot(partialTicks);
        this.head.rotationY = MathHelper.degToRad(-viewYaw - this.rotationYaw);
        this.head.rotationX = MathHelper.degToRad(this.rotationPitch);
        this.head.rotationZ = 0;
        if (isElytraFlying) {
            this.head.rotationX = -MathHelper.PI / 4;
        }
        else if (this.swimAnimation > 0.0F) {
            if (isVisuallySwimming) {
                if (!entity.isInWater()) {
                    this.head.rotationX = MathHelper.degToRad(this.rotationPitch + 90);
                }
                else {
                    this.head.rotationX = rotLerpRad(this.swimAnimation, this.head.rotationX, MathHelper.PI_OVER_2);
                }
                this.head.rotationY = 0;
                this.head.rotationZ = -MathHelper.degToRad(this.rotationYaw + viewYaw);
            }
            else {
                this.head.rotationX = rotLerpRad(this.swimAnimation, this.head.rotationX, MathHelper.degToRad(this.rotationPitch));
            }
        }
        float f = 1.0F;
        if (isElytraFlying) {
            f = (float) entity.getDeltaMovement().lengthSqr();
            f /= 0.2F;
            f = f * f * f;
        }
        if (f < 1.0F) {
            f = 1.0F;
        }
        this.rightArm.setRotationX(-MathHelper.cos(this.limbSwing * 0.666_2F + MathHelper.PI) * this.limbSwingAmount / f);
        this.leftArm.setRotationX(-MathHelper.cos(this.limbSwing * 0.666_2F) * this.limbSwingAmount / f);
        this.leftLeg.setRotationX(MathHelper.cos(this.limbSwing * 0.666_2F) * 1.4F * this.limbSwingAmount / f);
        this.rightLeg.setRotationX(MathHelper.cos(this.limbSwing * 0.666_2F + MathHelper.PI) * 1.4F * this.limbSwingAmount / f);
        if (this.isSitting) {
            this.rightArm.setRotationX(MathHelper.PI / 5);
            this.leftArm.setRotationX(MathHelper.PI / 5);
            this.leftLeg.setRotationX(1.413_716_7F);
            this.leftLeg.setRotationY(MathHelper.PI / 10);
            this.leftLeg.setRotationZ(-0.078_539_82F);
            this.rightLeg.setRotationX(1.413_716_7F);
            this.rightLeg.setRotationY(-MathHelper.PI / 10);
            this.rightLeg.setRotationZ(0.078_539_82F);
        }
        boolean isRightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        boolean isPoseTwoHanded = isRightHanded ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
        if (isRightHanded != isPoseTwoHanded) {
            this.poseLeftArm(entity);
            this.poseRightArm(entity);
        }
        else {
            this.poseRightArm(entity);
            this.poseLeftArm(entity);
        }
        this.setupAttackAnimation(entity);
        if (this.isSneak) {
            this.body.rotationX = -0.5F;
            this.rightArm.addRotationX(-0.4f);
            this.leftArm.addRotationX(-0.4f);
            this.rightLeg.setPivotZ(-4 * SCALE / 16);
            this.leftLeg.setPivotZ(-4 * SCALE / 16);
            this.rightLeg.setPivotY(9.75f * SCALE / 16);
            this.leftLeg.setPivotY(9.75f * SCALE / 16);
            this.head.pivotY = 16.5f / 16;
            this.body.pivotY = 17.5f / 16;
            this.leftArm.setPivotY(15.75f / 16);
            this.rightArm.setPivotY(15.75f / 16);
        }
        else {
            this.leftLeg.setPivotZ(-0.1f * SCALE / 16);
            this.rightLeg.setPivotZ(-0.1f * SCALE / 16);
        }
        this.bobArms(this.rightArm, this.leftArm, this.ageInTicks);
        if (this.swimAnimation > 0.0F) {
            float f7 = this.limbSwing % 26.0F;
            HumanoidArm attackArm = getAttackArm(entity);
            float rightArmAnim = attackArm == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAnimation;
            float leftArmAnim = attackArm == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAnimation;
            if (f7 < 14.0F) {
                this.leftArm.setRotationX(rotLerpRad(leftArmAnim, this.leftArm.getRotationX(), 0.0F));
                this.rightArm.setRotationX(Mth.lerp(rightArmAnim, this.rightArm.getRotationX(), 0.0F));
                this.leftArm.setRotationY(Mth.lerp(leftArmAnim, this.leftArm.getRotationY(), MathHelper.PI));
                this.rightArm.setRotationY(Mth.lerp(rightArmAnim, this.rightArm.getRotationY(), -MathHelper.PI));
                this.leftArm.setRotationZ(rotLerpRad(leftArmAnim,
                                                     this.leftArm.getRotationZ(),
                                                     MathHelper.PI + 1.870_796_4F * quadraticArmUpdate(f7) / quadraticArmUpdate(14.0F)));
                this.rightArm.setRotationZ(Mth.lerp(rightArmAnim,
                                                    this.rightArm.getRotationZ(),
                                                    MathHelper.PI - 1.870_796_4F * quadraticArmUpdate(f7) / quadraticArmUpdate(14.0F)));
            }
            else if (f7 >= 14.0F && f7 < 22.0F) {
                float f10 = (f7 - 14.0F) / 8.0F;
                this.leftArm.setRotationX(rotLerpRad(leftArmAnim, this.leftArm.getRotationX(), -MathHelper.PI_OVER_2 * f10));
                this.rightArm.setRotationX(-Mth.lerp(rightArmAnim, -this.rightArm.getRotationX(), MathHelper.PI_OVER_2 * f10));
                this.leftArm.setRotationY(Mth.lerp(leftArmAnim, this.leftArm.getRotationY(), MathHelper.PI));
                this.rightArm.setRotationY(Mth.lerp(rightArmAnim, this.rightArm.getRotationY(), -MathHelper.PI));
                this.leftArm.setRotationZ(rotLerpRad(leftArmAnim, this.leftArm.getRotationZ(), 5.012_389F - 1.870_796_4F * f10));
                this.rightArm.setRotationZ(Mth.lerp(rightArmAnim, this.rightArm.getRotationZ(), 1.270_796_3F + 1.870_796_4F * f10));
            }
            else if (f7 >= 22.0F && f7 < 26.0F) {
                float f9 = (f7 - 22.0F) / 4.0F;
                this.leftArm.setRotationX(-rotLerpRad(leftArmAnim, -this.leftArm.getRotationX(), MathHelper.PI_OVER_2 - MathHelper.PI_OVER_2 * f9));
                this.rightArm.setRotationX(-Mth.lerp(rightArmAnim, -this.rightArm.getRotationX(), MathHelper.PI_OVER_2 - MathHelper.PI_OVER_2 * f9));
                this.leftArm.setRotationY(Mth.lerp(leftArmAnim, this.leftArm.getRotationY(), MathHelper.PI));
                this.rightArm.setRotationY(Mth.lerp(rightArmAnim, this.rightArm.getRotationY(), -MathHelper.PI));
                this.leftArm.setRotationZ(rotLerpRad(leftArmAnim, this.leftArm.getRotationZ(), MathHelper.PI));
                this.rightArm.setRotationZ(Mth.lerp(rightArmAnim, this.rightArm.getRotationZ(), MathHelper.PI));
            }
            this.leftLeg.setRotationX(Mth.lerp(this.swimAnimation,
                                               this.leftLeg.getRotationX(),
                                               -0.3F * MathHelper.cos(this.limbSwing * 0.333_333_34F + MathHelper.PI)));
            this.rightLeg.setRotationX(Mth.lerp(this.swimAnimation,
                                                this.rightLeg.getRotationX(),
                                                -0.3F * MathHelper.cos(this.limbSwing * 0.333_333_34F)));
        }
    }

    private void poseLeftArm(Player entity) {
        switch (this.leftArmPose) {
            case EMPTY -> this.leftArm.setRotationY(0);
            case BLOCK -> {
                this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F + 0.942_477_9F);
                this.leftArm.setRotationY(-MathHelper.PI / 6.0F);
            }
            case ITEM -> {
                if (entity.isUsingItem()) {
                    if (shouldPoseArm(entity, HumanoidArm.LEFT)) {
                        ItemStack stack = entity.getUseItem();
                        Item useItem = stack.getItem();
                        UseAnim action = useItem.getUseAnimation(stack);
                        if (action == UseAnim.EAT || action == UseAnim.DRINK) {
                            this.leftArm.setRotationX(Mth.lerp(-1.0f * (entity.getXRot() - 90.0f) / 180.0f, 1.0f, 2.0f) -
                                                      MathHelper.sin(entity.tickCount * 1.5f) * 0.1f);
                            this.leftArm.setRotationY(-0.3f);
                            this.leftArm.setRotationZ(-0.3f);
                            break;
                        }
                    }
                }
                this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F + MathHelper.PI / 10.0F);
                this.leftArm.setRotationY(0);
            }
            case THROW_SPEAR -> {
                this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F + (MathHelper.PI - MathHelper.degToRad(entity.getXRot())));
                this.leftArm.setRotationY(0);
            }
            case BOW_AND_ARROW -> {
                this.rightArm.setRotationY(0.1F - this.head.rotationY + 0.4F);
                this.leftArm.setRotationY(-0.1F - this.head.rotationY);
                this.rightArm.setRotationX(MathHelper.PI_OVER_2 - this.head.rotationX);
                this.leftArm.setRotationX(MathHelper.PI_OVER_2 - this.head.rotationX);
            }
            case CROSSBOW_CHARGE -> this.animateCrossbowCharge(this.rightArm, this.leftArm, entity, false);
            case CROSSBOW_HOLD -> this.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
        }
    }

    private void poseRightArm(Player entity) {
        switch (this.rightArmPose) {
            case EMPTY -> this.rightArm.setRotationY(0);
            case BLOCK -> {
                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + 0.942_477_9F);
                this.rightArm.setRotationY(MathHelper.PI / 6.0F);
            }
            case ITEM -> {
                if (entity.isUsingItem()) {
                    if (shouldPoseArm(entity, HumanoidArm.RIGHT)) {
                        ItemStack stack = entity.getUseItem();
                        Item useItem = stack.getItem();
                        UseAnim action = useItem.getUseAnimation(stack);
                        if (action == UseAnim.EAT || action == UseAnim.DRINK) {
                            this.rightArm.setRotationX(Mth.lerp(-1.0f * (entity.getXRot() - 90.0f) / 180.0f, 1.0f, 2.0f) -
                                                       MathHelper.sin(entity.tickCount * 1.5f) * 0.1f);
                            this.rightArm.setRotationY(0.3f);
                            this.rightArm.setRotationZ(0.3f);
                            break;
                        }
                    }
                }
                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + MathHelper.PI / 10.0F);
                this.rightArm.setRotationY(0);
            }
            case THROW_SPEAR -> {
                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + (MathHelper.PI - MathHelper.degToRad(entity.getXRot())));
                this.rightArm.setRotationY(0);
            }
            case BOW_AND_ARROW -> {
                this.rightArm.setRotationY(0.1F - this.head.rotationY);
                this.leftArm.setRotationY(-0.1F - this.head.rotationY - 0.4F);
                this.rightArm.setRotationX(MathHelper.PI_OVER_2 - this.head.rotationX);
                this.leftArm.setRotationX(MathHelper.PI_OVER_2 - this.head.rotationX);
            }
            case CROSSBOW_CHARGE -> this.animateCrossbowCharge(this.rightArm, this.leftArm, entity, true);
            case CROSSBOW_HOLD -> this.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
        }
    }

    protected void setupAttackAnimation(Player player) {
        if (this.attackTime > 0.0F) {
            HumanoidArm handside = getAttackArm(player);
            if (!(((ILivingEntityPatch) player).renderMainhandCustomAttack() && player.getMainArm() == getAttackArm(player))) {
                HitboxGroup arm = this.getArmForSide(handside);
                float f = this.attackTime;
                this.body.rotationY = -MathHelper.sin(MathHelper.sqrt(f) * MathHelper.TAU) * 0.2F;
                if (handside == HumanoidArm.LEFT) {
                    this.body.rotationY *= -1.0F;
                }
                this.rightArm.setPivotZ(MathHelper.sin(this.body.rotationY) * 5.0F * SCALE / 16);
                this.rightArm.setPivotX(-MathHelper.cos(this.body.rotationY) * 5.0F * SCALE / 16);
                this.leftArm.setPivotZ(-MathHelper.sin(this.body.rotationY) * 5.0F * SCALE / 16);
                this.leftArm.setPivotX(MathHelper.cos(this.body.rotationY) * 5.0F * SCALE / 16);
                this.rightArm.addRotationY(this.body.rotationY);
                this.leftArm.addRotationY(this.body.rotationY);
                this.leftArm.addRotationX(this.body.rotationY);
                f = 1.0F - this.attackTime;
                f *= f;
                f *= f;
                f = 1.0F - f;
                float f1 = MathHelper.sin(f * MathHelper.PI);
                float f2 = MathHelper.sin(this.attackTime * MathHelper.PI) * -(-this.head.rotationX - 0.7F) * 0.75F;
                arm.setRotationX(arm.getRotationX() + (f1 * 1.2f + f2));
                arm.addRotationY(this.body.rotationY * 2.0F);
                arm.addRotationZ(MathHelper.sin(this.attackTime * MathHelper.PI) * -0.4F);
            }
        }
        if (((ILivingEntityPatch) player).renderMainhandCustomAttack()) {
            HumanoidArm attackingSide = player.getMainArm();
            HitboxGroup attackingArm = this.getArmForSide(attackingSide);
            float progress = ((ILivingEntityPatch) player).getMainhandCustomAttackProgress(1.0f);
            attackingArm.setRotationX(-MixinTempHelper.xRot(progress));
            attackingArm.setRotationY(MixinTempHelper.yRot(progress, this.head.rotationX));
            attackingArm.setRotationZ(MixinTempHelper.zRot(progress));
        }
    }
}
