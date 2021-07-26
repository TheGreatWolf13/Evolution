package tgw.evolution.util.hitbox;

import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
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
    protected boolean isSitting;
    protected boolean isSneak;
    protected ArmPose leftArmPose;
    protected float limbSwing;
    protected float limbSwingAmount;
    protected float remainingItemUseTime;
    protected ArmPose rightArmPose;
    protected float swimAnimation;
    protected float swingProgress;

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

    private static float func_203068_a(float f) {
        return -65.0F * f + f * f;
    }

    protected static HandSide getActiveHandside(PlayerEntity entity) {
        HandSide handside = entity.getPrimaryHand();
        return entity.getActiveHand() == Hand.MAIN_HAND ? handside : handside.opposite();
    }

    protected static HandSide getSwingingHandside(PlayerEntity entity) {
        HandSide handside = entity.getPrimaryHand();
        return entity.swingingHand == Hand.MAIN_HAND ? handside : handside.opposite();
    }

    protected static float swimInterp(float rotation, float pitch, float swimAnimation) {
        float f = (pitch - rotation) % MathHelper.TAU;
        if (f < -MathHelper.PI) {
            f += MathHelper.TAU;
        }
        if (f >= MathHelper.PI) {
            f -= MathHelper.TAU;
        }
        return rotation + swimAnimation * f;
    }

    private void eatingAnimationHand(PlayerEntity entity) {
        if (entity.isHandActive()) {
            ItemStack stack = entity.getHeldItem(entity.getActiveHand());
            boolean eatingOrDrinking = stack.getUseAction() == UseAction.EAT || stack.getUseAction() == UseAction.DRINK;
            if (entity.getItemInUseCount() > 0 && eatingOrDrinking) {
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

    @Override
    public void init(PlayerEntity entity, float partialTicks) {
        this.reset();
        this.rotationYaw = -MathHelper.getEntityBodyYaw(entity, partialTicks);
        this.rotationPitch = -entity.getPitch(partialTicks);
        this.limbSwing = MathHelper.getLimbSwing(entity, partialTicks);
        this.limbSwingAmount = MathHelper.getLimbSwingAmount(entity, partialTicks);
        this.swimAnimation = MathHelper.getSwimAnimation(entity, partialTicks);
        this.remainingItemUseTime = entity.getItemInUseMaxCount();
        this.isSitting = MathHelper.isSitting(entity);
        ItemStack mainhandStack = entity.getHeldItemMainhand();
        ItemStack offhandStack = entity.getHeldItemOffhand();
        ArmPose mainhandPose = MathHelper.getArmPose(entity, mainhandStack, offhandStack, Hand.MAIN_HAND);
        ArmPose offhandPose = MathHelper.getArmPose(entity, mainhandStack, offhandStack, Hand.OFF_HAND);
        if (entity.getPrimaryHand() == HandSide.RIGHT) {
            this.rightArmPose = mainhandPose;
            this.leftArmPose = offhandPose;
        }
        else {
            this.rightArmPose = offhandPose;
            this.leftArmPose = mainhandPose;
        }
        this.swingProgress = MathHelper.getSwingProgress(entity, partialTicks);
        this.isSneak = entity.getPose() == Pose.SNEAKING;
        this.ageInTicks = MathHelper.getAgeInTicks(entity, partialTicks);
        //Main
        this.rotationY = MathHelper.degToRad(this.rotationYaw);
        Pose pose = entity.getPose();
        if (pose == Pose.SLEEPING) {
            Direction direction = entity.getBedDirection();
            if (direction == null) {
                direction = Direction.WEST;
            }
            this.rotationY = MathHelper.degToRad(MathHelper.getAngleByDirection(direction) + 90);
            this.rotationX = MathHelper.degToRad(90);
            this.setPivot(-26 * direction.getXOffset(), 0, -26 * direction.getZOffset(), SCALE / 16);
        }
        else if (this.swimAnimation > 0) {
            float f3 = entity.isInWater() ? -90.0F - entity.rotationPitch : -90.0F;
            float f4 = MathHelper.lerp(this.swimAnimation, 0.0F, f3);
            this.rotationX += MathHelper.degToRad(f4);
            if (entity.isActualySwimming()) {
                this.pivotX = MathHelper.sinDeg(-this.rotationYaw) * -MathHelper.sinDeg(f4) +
                              5 / 16.0f * MathHelper.cosDeg(f4) * MathHelper.sinDeg(-this.rotationYaw);
                this.pivotY = 5 / 16.0f * -MathHelper.sinDeg(f4) - MathHelper.cosDeg(f4);
                this.pivotZ = -MathHelper.cosDeg(-this.rotationYaw) * -MathHelper.sinDeg(f4) -
                              5 / 16.0f * MathHelper.cosDeg(f4) * MathHelper.cosDeg(-this.rotationYaw);
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
        boolean isElytraFlying = entity.getTicksElytraFlying() > 4;
        boolean isActuallySwimming = entity.isActualySwimming();
        this.head.rotationY = MathHelper.degToRad(-entity.getYaw(partialTicks) - this.rotationYaw);
        this.head.rotationX = MathHelper.degToRad(this.rotationPitch);
        if (isElytraFlying) {
            this.head.rotationX = -MathHelper.PI / 4;
        }
        else if (this.swimAnimation > 0.0F) {
            if (isActuallySwimming) {
                this.head.rotationX = swimInterp(this.head.rotationX, MathHelper.PI / 4, this.swimAnimation);
            }
            else {
                this.head.rotationX = swimInterp(this.head.rotationX, MathHelper.degToRad(this.rotationPitch), this.swimAnimation);
            }
        }
        float f = 1.0F;
        if (isElytraFlying) {
            f = (float) entity.getMotion().lengthSquared();
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
        switch (this.leftArmPose) {
            case BLOCK:
                this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F + 0.942_477_9F);
                this.leftArm.setRotationY(-MathHelper.PI / 6);
                break;
            case ITEM:
                this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F + MathHelper.PI / 10);
                break;
        }
        switch (this.rightArmPose) {
            case BLOCK:
                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + 0.942_477_9F);
                this.rightArm.setRotationY(MathHelper.PI / 6);
                break;
            case ITEM:
                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + MathHelper.PI / 10);
                break;
            case THROW_SPEAR:
                this.rightArm.setRotationX(this.rightArm.getRotationX() * 0.5F + MathHelper.PI);
                break;
        }
        if (this.leftArmPose == ArmPose.THROW_SPEAR &&
            this.rightArmPose != ArmPose.BLOCK &&
            this.rightArmPose != ArmPose.THROW_SPEAR &&
            this.rightArmPose != ArmPose.BOW_AND_ARROW) {
            this.leftArm.setRotationX(this.leftArm.getRotationX() * 0.5F - MathHelper.PI);
        }
        if (this.swingProgress > 0) {
            HandSide swingingHandside = getSwingingHandside(entity);
            HitboxGroup swingingArm = this.getArmForSide(swingingHandside);
            float f1 = this.swingProgress;
            this.body.rotationY = -MathHelper.sin(MathHelper.sqrt(f1) * MathHelper.TAU) * 0.2F;
            if (swingingHandside == HandSide.LEFT) {
                this.body.rotationY *= -1;
            }
            this.rightArm.setPivotZ(MathHelper.sin(this.body.rotationY) * 5.0F * SCALE / 16);
            this.rightArm.setPivotX(-MathHelper.cos(this.body.rotationY) * 5.0F * SCALE / 16);
            this.leftArm.setPivotZ(-MathHelper.sin(this.body.rotationY) * 5.0F * SCALE / 16);
            this.leftArm.setPivotX(MathHelper.cos(this.body.rotationY) * 5.0F * SCALE / 16);
            this.rightArm.addRotationY(this.body.rotationY);
            this.leftArm.addRotationY(this.body.rotationY);
            this.leftArm.addRotationX(this.body.rotationY);
            f1 = 1.0F - this.swingProgress;
            f1 *= f1;
            f1 *= f1;
            f1 = 1.0F - f1;
            float f2 = MathHelper.sin(f1 * MathHelper.PI);
            float f3 = MathHelper.sin(this.swingProgress * MathHelper.PI) * -(-this.head.rotationX - 0.7F) * 0.75F;
            swingingArm.setRotationX(swingingArm.getRotationX() + (f2 * 1.2F + f3));
            swingingArm.addRotationY(this.body.rotationY * 2);
            swingingArm.addRotationZ(MathHelper.sin(this.swingProgress * MathHelper.PI) * -0.4F);
        }
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
        this.rightArm.addRotationZ(MathHelper.cos(this.ageInTicks * 0.09F) * 0.05F + 0.05F);
        this.leftArm.addRotationZ(-MathHelper.cos(this.ageInTicks * 0.09F) * 0.05F - 0.05F);
        this.rightArm.addRotationX(-MathHelper.sin(this.ageInTicks * 0.067F) * 0.05F);
        this.leftArm.addRotationX(MathHelper.sin(this.ageInTicks * 0.067F) * 0.05F);
        if (this.rightArmPose == ArmPose.BOW_AND_ARROW) {
            this.rightArm.setRotationY(0.1F + this.head.rotationY);
            this.leftArm.setRotationY(-0.5F + this.head.rotationY);
            this.rightArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX);
            this.leftArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX);
        }
        else if (this.leftArmPose == ArmPose.BOW_AND_ARROW && this.rightArmPose != ArmPose.THROW_SPEAR && this.rightArmPose != ArmPose.BLOCK) {
            this.rightArm.setRotationY(0.5F + this.head.rotationY);
            this.leftArm.setRotationY(-0.1F + this.head.rotationY);
            this.rightArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX);
            this.leftArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX);
        }
        float f4 = CrossbowItem.getChargeTime(entity.getActiveItemStack());
        if (this.rightArmPose == ArmPose.CROSSBOW_CHARGE) {
            this.rightArm.setRotationY(0.8F);
            this.rightArm.setRotationX(0.970_796_35F);
            this.leftArm.setRotationX(-0.970_796_35F);
            float f5 = MathHelper.clamp(this.remainingItemUseTime, 0.0F, f4);
            this.leftArm.setRotationY(-MathHelper.lerp(f5 / f4, 0.4F, 0.85F));
            this.leftArm.setRotationX(-MathHelper.lerp(f5 / f4, this.leftArm.getRotationX(), -MathHelper.PI / 2));
        }
        else if (this.leftArmPose == ArmPose.CROSSBOW_CHARGE) {
            this.leftArm.setRotationY(-0.8F);
            this.rightArm.setRotationX(-0.970_796_35F);
            this.leftArm.setRotationX(0.970_796_35F);
            float f6 = MathHelper.clamp(this.remainingItemUseTime, 0.0F, f4);
            this.rightArm.setRotationY(-MathHelper.lerp(f6 / f4, -0.4F, -0.85F));
            this.rightArm.setRotationX(-MathHelper.lerp(f6 / f4, this.rightArm.getRotationX(), -MathHelper.PI / 2));
        }
        if (this.rightArmPose == ArmPose.CROSSBOW_HOLD && this.swingProgress <= 0.0F) {
            this.rightArm.setRotationY(0.3F + this.head.rotationY);
            this.leftArm.setRotationY(-0.6F + this.head.rotationY);
            this.rightArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX - 0.1F);
            this.leftArm.setRotationX(1.5F + this.head.rotationX);
        }
        else if (this.leftArmPose == ArmPose.CROSSBOW_HOLD) {
            this.rightArm.setRotationY(0.6F + this.head.rotationY);
            this.leftArm.setRotationY(-0.3F + this.head.rotationY);
            this.rightArm.setRotationX(1.5F + this.head.rotationX);
            this.leftArm.setRotationX(MathHelper.PI / 2 + this.head.rotationX - 0.1F);
        }
        if (this.swimAnimation > 0.0F) {
            float f7 = this.limbSwing % 26.0F;
            float f8 = this.swingProgress > 0.0F ? 0.0F : this.swimAnimation;
            if (f7 < 14.0F) {
                this.leftArm.setRotationX(swimInterp(this.leftArm.getRotationX(), 0.0F, this.swimAnimation));
                this.rightArm.setRotationX(MathHelper.lerp(f8, this.rightArm.getRotationX(), 0.0F));
                this.leftArm.setRotationY(-swimInterp(-this.leftArm.getRotationY(), MathHelper.PI, this.swimAnimation));
                this.rightArm.setRotationY(-MathHelper.lerp(f8, -this.rightArm.getRotationY(), MathHelper.PI));
                this.leftArm.setRotationZ(swimInterp(this.leftArm.getRotationZ(),
                                                     MathHelper.PI + 1.870_796_4F * func_203068_a(f7) / func_203068_a(14.0F),
                                                     this.swimAnimation));
                this.rightArm.setRotationZ(MathHelper.lerp(f8,
                                                           this.rightArm.getRotationZ(),
                                                           MathHelper.PI - 1.870_796_4F * func_203068_a(f7) / func_203068_a(14.0F)));
            }
            else if (f7 >= 14.0F && f7 < 22.0F) {
                float f10 = (f7 - 14.0F) / 8.0F;
                this.leftArm.setRotationX(swimInterp(this.leftArm.getRotationX(), -MathHelper.PI / 2 * f10, this.swimAnimation));
                this.rightArm.setRotationX(MathHelper.lerp(f8, this.rightArm.getRotationX(), -MathHelper.PI / 2 * f10));
                this.leftArm.setRotationY(-swimInterp(-this.leftArm.getRotationY(), MathHelper.PI, this.swimAnimation));
                this.rightArm.setRotationY(-MathHelper.lerp(f8, -this.rightArm.getRotationY(), MathHelper.PI));
                this.leftArm.setRotationZ(swimInterp(this.leftArm.getRotationZ(), 5.012_389F - 1.870_796_4F * f10, this.swimAnimation));
                this.rightArm.setRotationZ(MathHelper.lerp(f8, this.rightArm.getRotationZ(), 1.270_796_3F + 1.870_796_4F * f10));
            }
            else if (f7 >= 22.0F && f7 < 26.0F) {
                float f9 = (f7 - 22.0F) / 4.0F;
                this.leftArm.setRotationX(swimInterp(this.leftArm.getRotationX(), -MathHelper.PI / 2 + MathHelper.PI / 2 * f9, this.swimAnimation));
                this.rightArm.setRotationX(MathHelper.lerp(f8, this.rightArm.getRotationX(), -MathHelper.PI / 2 + MathHelper.PI / 2 * f9));
                this.leftArm.setRotationY(-swimInterp(-this.leftArm.getRotationY(), MathHelper.PI, this.swimAnimation));
                this.rightArm.setRotationY(-MathHelper.lerp(f8, -this.rightArm.getRotationY(), MathHelper.PI));
                this.leftArm.setRotationZ(swimInterp(this.leftArm.getRotationZ(), MathHelper.PI, this.swimAnimation));
                this.rightArm.setRotationZ(MathHelper.lerp(f8, this.rightArm.getRotationZ(), MathHelper.PI));
            }
            this.leftLeg.setRotationX(MathHelper.lerp(this.swimAnimation,
                                                      this.leftLeg.getRotationX(),
                                                      -0.3F * MathHelper.cos(this.limbSwing * 0.333_333_34F + MathHelper.PI)));
            this.rightLeg.setRotationX(MathHelper.lerp(this.swimAnimation,
                                                       this.rightLeg.getRotationX(),
                                                       -0.3F * MathHelper.cos(this.limbSwing * 0.333_333_34F)));
        }
        //TODO add lunge
        this.eatingAnimationHand(entity);
    }
}
