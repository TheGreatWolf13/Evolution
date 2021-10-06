package tgw.evolution.util.hitbox;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.MathHelper;

public class HitboxPlayer extends HitboxEntity<PlayerEntity> {

    protected static final float SCALE = 0.937_5f;
    protected final Hitbox armL;
    protected final Hitbox armR;
    protected final Hitbox body = this.addBox(BodyPart.CHEST, aabb(-4, -12, -2, 4, 0, 2, SCALE));
    protected final Hitbox footL = this.addBox(BodyPart.LEFT_FOOT, HitboxLib.PLAYER_FOOT);
    protected final Hitbox footR = this.addBox(BodyPart.RIGHT_FOOT, HitboxLib.PLAYER_FOOT);
    protected final Hitbox handL;
    protected final Hitbox handR;
    protected final Hitbox head = this.addBox(BodyPart.HEAD, aabb(-4, 0, -4, 4, 8, 4, SCALE));
    protected final HitboxGroup leftArm;
    protected final HitboxGroup leftLeg;
    protected final Hitbox legL = this.addBox(BodyPart.LEFT_LEG, HitboxLib.PLAYER_LEG);
    protected final Hitbox legR = this.addBox(BodyPart.RIGHT_LEG, HitboxLib.PLAYER_LEG);
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
        this.armL = this.addBox(BodyPart.LEFT_ARM, aabb(-1, -6, -2, slim ? 2 : 3, -2, 2, SCALE));
        this.shoulderL = this.addBox(BodyPart.LEFT_SHOULDER, aabb(-1, -2, -2, slim ? 2 : 3, 2, 2, SCALE));
        this.handL = this.addBox(BodyPart.LEFT_HAND, aabb(-1, -10, -2, slim ? 2 : 3, -6, 2, SCALE));
        this.armR = this.addBox(BodyPart.RIGHT_ARM, aabb(slim ? -2 : -3, -6, -2, 1, -2, 2, SCALE));
        this.shoulderR = this.addBox(BodyPart.RIGHT_SHOULDER, aabb(slim ? -2 : -3, -2, -2, 1, 2, 2, SCALE));
        this.handR = this.addBox(BodyPart.RIGHT_HAND, aabb(slim ? -2 : -3, -10, -2, 1, -6, 2, SCALE));
        this.leftArm = new HitboxGroup(this.shoulderL, this.armL, this.handL);
        this.leftLeg = new HitboxGroup(this.legL, this.footL);
        this.rightArm = new HitboxGroup(this.shoulderR, this.armR, this.handR);
        this.rightLeg = new HitboxGroup(this.legR, this.footR);
    }

    protected static HandSide getActiveHandside(PlayerEntity entity) {
        HandSide handside = entity.getMainArm();
        return entity.getUsedItemHand() == Hand.MAIN_HAND ? handside : handside.getOpposite();
    }

    protected static HandSide getSwingingHandside(PlayerEntity entity) {
        HandSide handside = entity.getMainArm();
        return entity.swingingArm == Hand.MAIN_HAND ? handside : handside.getOpposite();
    }

    private static float quadraticArmUpdate(float f) {
        return -65.0F * f + f * f;
    }

    protected static float rotLerpRad(float partialTick, float old, float now) {
        float f = (now - old) % MathHelper.TAU;
        if (f < -MathHelper.PI) {
            f += MathHelper.TAU;
        }
        if (f >= MathHelper.PI) {
            f -= MathHelper.TAU;
        }
        return old + partialTick * f;
    }

    private void eatingAnimationHand(PlayerEntity entity) {
        if (entity.isUsingItem()) {
            ItemStack stack = entity.getItemInHand(entity.getUsedItemHand());
            boolean eatingOrDrinking = stack.getUseAnimation() == UseAction.EAT || stack.getUseAnimation() == UseAction.DRINK;
            if (eatingOrDrinking && entity.getTicksUsingItem() > 0) {
                HandSide activeHandside = getActiveHandside(entity);
                HitboxGroup eatingArm = this.getArmForSide(activeHandside);
                int mult = activeHandside == HandSide.LEFT ? -1 : 1;
                eatingArm.setRotationX(1.3f);
                eatingArm.setRotationY(0.5f * mult);
                eatingArm.setRotationZ(MathHelper.cos(this.ageInTicks) * 0.1F);
                this.head.rotationY = 0;
                this.head.rotationX = -MathHelper.cos(this.ageInTicks) * 0.2F;
            }
        }
    }

    protected HitboxGroup getArmForSide(HandSide side) {
        return side == HandSide.LEFT ? this.leftArm : this.rightArm;
    }

    protected HandSide getAttackArm(LivingEntity entity) {
        HandSide handside = entity.getMainArm();
        return entity.swingingArm == Hand.MAIN_HAND ? handside : handside.getOpposite();
    }

    @Override
    public void init(PlayerEntity entity, float partialTicks) {
        this.reset();
        this.rotationYaw = -MathHelper.getEntityBodyYaw(entity, partialTicks);
        if (entity.getVehicle() instanceof AbstractHorseEntity) {
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
        this.rightArmPose = MathHelper.getArmPose(entity, mainhandStack, offhandStack, Hand.MAIN_HAND);
        this.leftArmPose = MathHelper.getArmPose(entity, mainhandStack, offhandStack, Hand.OFF_HAND);
//        if (entity.getMainArm() == HandSide.RIGHT) {
//            this.rightArmPose = mainhandPose;
//            this.leftArmPose = offhandPose;
//        }
//        else {
//            this.rightArmPose = offhandPose;
//            this.leftArmPose = mainhandPose;
//        }
        this.attackTime = MathHelper.getAttackAnim(entity, partialTicks);
        this.isSneak = entity.getPose() == Pose.CROUCHING;
        this.ageInTicks = MathHelper.getAgeInTicks(entity, partialTicks);
        //Main
        this.rotationY = MathHelper.degToRad(this.rotationYaw);
        float sinYaw = MathHelper.sinDeg(this.rotationYaw);
        float cosYaw = MathHelper.cosDeg(this.rotationYaw);
        Pose pose = entity.getPose();
        switch (pose) {
            case CROUCHING:
            case STANDING: {
                this.pivotX = -1 / 16.0f * sinYaw * (1 - this.swimAnimation);
                this.pivotZ = -1 / 16.0f * cosYaw * (1 - this.swimAnimation);
                break;
            }
            case SLEEPING: {
                Direction direction = entity.getBedOrientation();
                if (direction == null) {
                    direction = Direction.WEST;
                }
                this.rotationY = MathHelper.degToRad(MathHelper.getAngleByDirection(direction) + 90);
                this.rotationX = MathHelper.degToRad(90);
                this.setPivot(-26 * direction.getStepX(), 0, -26 * direction.getStepZ(), SCALE / 16);
                break;
            }
        }
        if (this.swimAnimation > 0) {
            if (!entity.isInWater()) {
                float waterInclination = -90.0F;
                float waterPitch = MathHelper.lerp(this.swimAnimation, 0.0F, waterInclination);
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
                float waterInclination = -90.0F - entity.xRot;
                float waterPitch = MathHelper.lerp(this.swimAnimation, 0.0F, waterInclination);
                this.rotationX += MathHelper.degToRad(waterPitch);
                if (entity.isVisuallySwimming()) {
                    float sinWaterPitch = MathHelper.sinDeg(waterPitch);
                    float cosWaterPitch = MathHelper.cosDeg(waterPitch);
                    this.pivotX = -sinYaw * -sinWaterPitch + 5 / 16.0f * cosWaterPitch * -sinYaw;
                    this.pivotY = 5 / 16.0f * -sinWaterPitch - cosWaterPitch;
                    this.pivotZ = -cosYaw * -sinWaterPitch - 5 / 16.0f * cosWaterPitch * cosYaw;
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
                    this.head.rotationX = rotLerpRad(this.swimAnimation, this.head.rotationX, -MathHelper.PI_OVER_2);
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
        boolean isRightHanded = entity.getMainArm() == HandSide.RIGHT;
        boolean isPoseTwoHanded = isRightHanded ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
        if (isRightHanded != isPoseTwoHanded) {
            this.poseLeftArm(entity);
            this.poseRightArm(entity);
        }
        else {
            this.poseRightArm(entity);
            this.poseLeftArm(entity);
        }
        this.setupAttackAnimation(entity, this.ageInTicks);
//        switch (this.leftArmPose) {
//            case BLOCK:
//                this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F + 0.942_477_9F);
//                this.leftArm.setRotationY(-MathHelper.PI / 6);
//                break;
//            case ITEM:
//                this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F + MathHelper.PI / 10);
//                break;
//        }
//        switch (this.rightArmPose) {
//            case BLOCK:
//                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + 0.942_477_9F);
//                this.rightArm.setRotationY(MathHelper.PI / 6);
//                break;
//            case ITEM:
//                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + MathHelper.PI / 10);
//                break;
//            case THROW_SPEAR:
//                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + MathHelper.PI);
//                break;
//        }
//        if (this.leftArmPose == ArmPose.THROW_SPEAR &&
//            this.rightArmPose != ArmPose.BLOCK &&
//            this.rightArmPose != ArmPose.THROW_SPEAR &&
//            this.rightArmPose != ArmPose.BOW_AND_ARROW) {
//            this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F - MathHelper.PI);
//        }
//        if (this.attackTime > 0) {
//            HandSide swingingHandside = getSwingingHandside(entity);
//            HitboxGroup swingingArm = this.getArmForSide(swingingHandside);
//            float f1 = this.attackTime;
//            this.body.rotationY = -MathHelper.sin(MathHelper.sqrt(f1) * MathHelper.TAU) * 0.2F;
//            if (swingingHandside == HandSide.LEFT) {
//                this.body.rotationY *= -1;
//            }
//            this.rightArm.setPivotZ(MathHelper.sin(this.body.rotationY) * 5.0F * SCALE / 16);
//            this.rightArm.setPivotX(-MathHelper.cos(this.body.rotationY) * 5.0F * SCALE / 16);
//            this.leftArm.setPivotZ(-MathHelper.sin(this.body.rotationY) * 5.0F * SCALE / 16);
//            this.leftArm.setPivotX(MathHelper.cos(this.body.rotationY) * 5.0F * SCALE / 16);
//            this.rightArm.addRotationY(this.body.rotationY);
//            this.leftArm.addRotationY(this.body.rotationY);
//            this.leftArm.addRotationX(this.body.rotationY);
//            f1 = 1.0F - this.attackTime;
//            f1 *= f1;
//            f1 *= f1;
//            f1 = 1.0F - f1;
//            float f2 = MathHelper.sin(f1 * MathHelper.PI);
//            float f3 = MathHelper.sin(this.attackTime * MathHelper.PI) * -(-this.head.rotationX - 0.7F) * 0.75F;
//            swingingArm.setRotationX(swingingArm.getRotationX() + (f2 * 1.2F + f3));
//            swingingArm.addRotationY(this.body.rotationY * 2);
//            swingingArm.addRotationZ(MathHelper.sin(this.attackTime * MathHelper.PI) * -0.4F);
//        }
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
//        if (this.rightArmPose == ArmPose.BOW_AND_ARROW) {
//            this.rightArm.setRotationY(0.1F + this.head.rotationY);
//            this.leftArm.setRotationY(-0.5F + this.head.rotationY);
//            this.rightArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX);
//            this.leftArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX);
//        }
//        else if (this.leftArmPose == ArmPose.BOW_AND_ARROW && this.rightArmPose != ArmPose.THROW_SPEAR && this.rightArmPose != ArmPose.BLOCK) {
//            this.rightArm.setRotationY(0.5F + this.head.rotationY);
//            this.leftArm.setRotationY(-0.1F + this.head.rotationY);
//            this.rightArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX);
//            this.leftArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX);
//        }
//        float f4 = CrossbowItem.getChargeDuration(entity.getUseItem());
//        if (this.rightArmPose == ArmPose.CROSSBOW_CHARGE) {
//            this.rightArm.setRotationY(0.8F);
//            this.rightArm.setRotationX(0.970_796_35F);
//            this.leftArm.setRotationX(-0.970_796_35F);
//            float f5 = MathHelper.clamp(this.remainingItemUseTime, 0.0F, f4);
//            this.leftArm.setRotationY(-MathHelper.lerp(f5 / f4, 0.4F, 0.85F));
//            this.leftArm.setRotationX(-MathHelper.lerp(f5 / f4, this.leftArm.getRotationX(), -MathHelper.PI / 2));
//        }
//        else if (this.leftArmPose == ArmPose.CROSSBOW_CHARGE) {
//            this.leftArm.setRotationY(-0.8F);
//            this.rightArm.setRotationX(-0.970_796_35F);
//            this.leftArm.setRotationX(0.970_796_35F);
//            float f6 = MathHelper.clamp(this.remainingItemUseTime, 0.0F, f4);
//            this.rightArm.setRotationY(-MathHelper.lerp(f6 / f4, -0.4F, -0.85F));
//            this.rightArm.setRotationX(-MathHelper.lerp(f6 / f4, this.rightArm.getRotationX(), -MathHelper.PI / 2));
//        }
//        if (this.rightArmPose == ArmPose.CROSSBOW_HOLD && this.attackTime <= 0.0F) {
//            this.rightArm.setRotationY(0.3F + this.head.rotationY);
//            this.leftArm.setRotationY(-0.6F + this.head.rotationY);
//            this.rightArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX - 0.1F);
//            this.leftArm.setRotationX(1.5F + this.head.rotationX);
//        }
//        else if (this.leftArmPose == ArmPose.CROSSBOW_HOLD) {
//            this.rightArm.setRotationY(0.6F + this.head.rotationY);
//            this.leftArm.setRotationY(-0.3F + this.head.rotationY);
//            this.rightArm.setRotationX(1.5F + this.head.rotationX);
//            this.leftArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX - 0.1F);
//        }
        if (this.swimAnimation > 0.0F) {
            float f7 = this.limbSwing % 26.0F;
            HandSide attackArm = this.getAttackArm(entity);
            float rightArmAnim = attackArm == HandSide.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAnimation;
            float leftArmAnim = attackArm == HandSide.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAnimation;
            if (f7 < 14.0F) {
                this.leftArm.setRotationX(rotLerpRad(leftArmAnim, this.leftArm.getRotationX(), 0.0F));
                this.rightArm.setRotationX(MathHelper.lerp(rightArmAnim, this.rightArm.getRotationX(), 0.0F));
                this.leftArm.setRotationY(MathHelper.lerp(leftArmAnim, this.leftArm.getRotationY(), MathHelper.PI));
                this.rightArm.setRotationY(MathHelper.lerp(rightArmAnim, this.rightArm.getRotationY(), -MathHelper.PI));
                this.leftArm.setRotationZ(rotLerpRad(leftArmAnim,
                                                     this.leftArm.getRotationZ(),
                                                     MathHelper.PI + 1.870_796_4F * quadraticArmUpdate(f7) / quadraticArmUpdate(14.0F)));
                this.rightArm.setRotationZ(MathHelper.lerp(rightArmAnim,
                                                           this.rightArm.getRotationZ(),
                                                           MathHelper.PI - 1.870_796_4F * quadraticArmUpdate(f7) / quadraticArmUpdate(14.0F)));
            }
            else if (f7 >= 14.0F && f7 < 22.0F) {
                float f10 = (f7 - 14.0F) / 8.0F;
                this.leftArm.setRotationX(rotLerpRad(leftArmAnim, this.leftArm.getRotationX(), -MathHelper.PI_OVER_2 * f10));
                this.rightArm.setRotationX(-MathHelper.lerp(rightArmAnim, -this.rightArm.getRotationX(), MathHelper.PI_OVER_2 * f10));
                this.leftArm.setRotationY(MathHelper.lerp(leftArmAnim, this.leftArm.getRotationY(), MathHelper.PI));
                this.rightArm.setRotationY(MathHelper.lerp(rightArmAnim, this.rightArm.getRotationY(), -MathHelper.PI));
                this.leftArm.setRotationZ(rotLerpRad(leftArmAnim, this.leftArm.getRotationZ(), 5.012_389F - 1.870_796_4F * f10));
                this.rightArm.setRotationZ(MathHelper.lerp(rightArmAnim, this.rightArm.getRotationZ(), 1.270_796_3F + 1.870_796_4F * f10));
            }
            else if (f7 >= 22.0F && f7 < 26.0F) {
                float f9 = (f7 - 22.0F) / 4.0F;
                this.leftArm.setRotationX(-rotLerpRad(leftArmAnim, -this.leftArm.getRotationX(), MathHelper.PI_OVER_2 - MathHelper.PI_OVER_2 * f9));
                this.rightArm.setRotationX(-MathHelper.lerp(rightArmAnim,
                                                            -this.rightArm.getRotationX(),
                                                            MathHelper.PI_OVER_2 - MathHelper.PI_OVER_2 * f9));
                this.leftArm.setRotationY(MathHelper.lerp(leftArmAnim, this.leftArm.getRotationY(), MathHelper.PI));
                this.rightArm.setRotationY(MathHelper.lerp(rightArmAnim, this.rightArm.getRotationY(), -MathHelper.PI));
                this.leftArm.setRotationZ(rotLerpRad(leftArmAnim, this.leftArm.getRotationZ(), MathHelper.PI));
                this.rightArm.setRotationZ(MathHelper.lerp(rightArmAnim, this.rightArm.getRotationZ(), MathHelper.PI));
            }
            this.leftLeg.setRotationX(MathHelper.lerp(this.swimAnimation,
                                                      this.leftLeg.getRotationX(),
                                                      -0.3F * MathHelper.cos(this.limbSwing * 0.333_333_34F + MathHelper.PI)));
            this.rightLeg.setRotationX(MathHelper.lerp(this.swimAnimation,
                                                       this.rightLeg.getRotationX(),
                                                       -0.3F * MathHelper.cos(this.limbSwing * 0.333_333_34F)));
        }
        //TODO add lunge
//        this.eatingAnimationHand(entity);
    }

    private void poseLeftArm(PlayerEntity entity) {
        switch (this.leftArmPose) {
            case EMPTY: {
                this.leftArm.setRotationY(0);
                break;
            }
            case BLOCK: {
                this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F + 0.942_477_9F);
                this.leftArm.setRotationY(-MathHelper.PI / 6.0F);
                break;
            }
            case ITEM: {
                if (entity.isUsingItem()) {
                    if (shouldPoseArm(entity, HandSide.LEFT)) {
                        ItemStack stack = entity.getUseItem();
                        Item useItem = stack.getItem();
                        UseAction action = useItem.getUseAnimation(stack);
                        if (action == UseAction.EAT || action == UseAction.DRINK) {
                            this.leftArm.setRotationX(MathHelper.lerp(-1.0f * (entity.xRot - 90.0f) / 180.0f, 1.0f, 2.0f) -
                                                      MathHelper.sin(entity.tickCount * 1.5f) * 0.1f);
                            this.leftArm.setRotationY(-0.3f);
                            this.leftArm.setRotationZ(-0.3f);
                            break;
                        }
                    }
                }
                this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F + MathHelper.PI / 10.0F);
                this.leftArm.setRotationY(0);
                break;
            }
            case THROW_SPEAR: {
                this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F + (MathHelper.PI - MathHelper.degToRad(entity.xRot)));
                this.leftArm.setRotationY(0);
                break;
            }
            case BOW_AND_ARROW: {
                this.rightArm.setRotationY(0.1F - this.head.rotationY + 0.4F);
                this.leftArm.setRotationY(-0.1F - this.head.rotationY);
                this.rightArm.setRotationX(MathHelper.PI_OVER_2 - this.head.rotationX);
                this.leftArm.setRotationX(MathHelper.PI_OVER_2 - this.head.rotationX);
                break;
            }
            case CROSSBOW_CHARGE: {
                this.animateCrossbowCharge(this.rightArm, this.leftArm, entity, false);
                break;
            }
            case CROSSBOW_HOLD: {
                this.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
                break;
            }
        }
    }

    private void poseRightArm(PlayerEntity entity) {
        switch (this.rightArmPose) {
            case EMPTY: {
                this.rightArm.setRotationY(0);
                break;
            }
            case BLOCK: {
                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + 0.942_477_9F);
                this.rightArm.setRotationY(MathHelper.PI / 6.0F);
                break;
            }
            case ITEM: {
                if (entity.isUsingItem()) {
                    if (shouldPoseArm(entity, HandSide.RIGHT)) {
                        ItemStack stack = entity.getUseItem();
                        Item useItem = stack.getItem();
                        UseAction action = useItem.getUseAnimation(stack);
                        if (action == UseAction.EAT || action == UseAction.DRINK) {
                            this.rightArm.setRotationX(MathHelper.lerp(-1.0f * (entity.xRot - 90.0f) / 180.0f, 1.0f, 2.0f) -
                                                       MathHelper.sin(entity.tickCount * 1.5f) * 0.1f);
                            this.rightArm.setRotationY(0.3f);
                            this.rightArm.setRotationZ(0.3f);
                            break;
                        }
                    }
                }
                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + MathHelper.PI / 10.0F);
                this.rightArm.setRotationY(0);
                break;
            }
            case THROW_SPEAR: {
                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + (MathHelper.PI - MathHelper.degToRad(entity.xRot)));
                this.rightArm.setRotationY(0);
                break;
            }
            case BOW_AND_ARROW: {
                this.rightArm.setRotationY(0.1F - this.head.rotationY);
                this.leftArm.setRotationY(-0.1F - this.head.rotationY - 0.4F);
                this.rightArm.setRotationX(MathHelper.PI_OVER_2 - this.head.rotationX);
                this.leftArm.setRotationX(MathHelper.PI_OVER_2 - this.head.rotationX);
                break;
            }
            case CROSSBOW_CHARGE: {
                this.animateCrossbowCharge(this.rightArm, this.leftArm, entity, true);
                break;
            }
            case CROSSBOW_HOLD: {
                this.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
                break;
            }
        }
    }

    protected void setupAttackAnimation(PlayerEntity player, float ageInTicks) {
        if (!(this.attackTime <= 0.0F)) {
            HandSide handside = this.getAttackArm(player);
            HitboxGroup arm = this.getArmForSide(handside);
            float f = this.attackTime;
            this.body.rotationY = -MathHelper.sin(MathHelper.sqrt(f) * MathHelper.TAU) * 0.2F;
            if (handside == HandSide.LEFT) {
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
}
