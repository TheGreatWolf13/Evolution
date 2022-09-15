package tgw.evolution.util.hitbox.hms;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.math.MathHelper;

public interface HMHumanoid<T extends LivingEntity> extends HMAgeableList<T> {

    private static float quadraticArmUpdate(float limbSwing) {
        return -65.0F * limbSwing + limbSwing * limbSwing;
    }

    private static float rotlerpRad(float angle, float maxAngle, float mul) {
        float f = (mul - maxAngle) % (Mth.PI * 2.0F);
        if (f < -Mth.PI) {
            f += Mth.PI * 2.0F;
        }
        if (f >= Mth.PI) {
            f -= Mth.PI * 2.0F;
        }
        return maxAngle + angle * f;
    }

    default HM arm(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? this.leftArm() : this.rightArm();
    }

    HM body();

    boolean crouching();

    private HumanoidArm getAttackArm(T entity) {
        HumanoidArm arm = entity.getMainArm();
        return entity.swingingArm == InteractionHand.MAIN_HAND ? arm : arm.getOpposite();
    }

    HM hat();

    HM head();

    HM leftArm();

    ArmPose leftArmPose();

    HM leftLeg();

    private boolean poseLeftArm(T entity) {
        boolean shouldCancelFutureRepositions = false;
        switch (this.leftArmPose()) {
            case EMPTY -> this.leftArm().setRotationY(0.0F);
            case BLOCK -> {
                this.leftArm().setRotationX(this.leftArm().xRot() * 0.5F + 0.3f * Mth.PI);
                this.leftArm().setRotationY(-Mth.PI / 6.0F);
            }
            case ITEM -> {
                if (entity.isUsingItem()) {
                    if (this.shouldPoseArm(entity, HumanoidArm.LEFT)) {
                        ItemStack stack = entity.getUseItem();
                        Item useItem = stack.getItem();
                        UseAnim action = useItem.getUseAnimation(stack);
                        if (action == UseAnim.EAT || action == UseAnim.DRINK) {
                            this.leftArm().setRotationX(-Mth.lerp(-1.0f * (entity.getXRot() - 90.0f) / 180.0f, 1.0f, 2.0f) -
                                                        Mth.sin(entity.tickCount * 1.5f) * 0.1f);
                            this.leftArm().setRotationY(-0.3f);
                            this.leftArm().setRotationZ(-0.3f);
                            return true;
                        }
                    }
                }
                this.leftArm().setRotationX(this.leftArm().xRot() * 0.5F + Mth.PI / 10.0F);
                this.leftArm().setRotationY(0.0F);
            }
            case THROW_SPEAR -> {
                if (this.swimAmount() > 0 && entity.isInWater()) {
                    this.leftArm().setRotationX(this.leftArm().xRot() * 0.5F + Mth.PI);
                }
                else {
                    this.leftArm().setRotationX(this.leftArm().xRot() * 0.5F + (Mth.PI - entity.getXRot() * Mth.DEG_TO_RAD));
                }
                this.leftArm().setRotationY(0.0F);
                shouldCancelFutureRepositions = true;
            }
            case BOW_AND_ARROW -> {
                this.rightArm().setRotationY(0.1F - this.head().yRot() + 0.4F);
                this.leftArm().setRotationY(-0.1F - this.head().yRot());
                this.rightArm().setRotationX(Mth.HALF_PI - this.head().xRot());
                this.leftArm().setRotationX(Mth.HALF_PI - this.head().xRot());
            }
            case CROSSBOW_CHARGE -> AnimationUtils.animateCrossbowCharge(this.rightArm(), this.leftArm(), entity, false);
            case CROSSBOW_HOLD -> AnimationUtils.animateCrossbowHold(this.rightArm(), this.leftArm(), this.head(), false);
            case SPYGLASS -> {
                this.leftArm().setRotationX(Mth.clamp(this.head().xRot() + 1.919_862_2F + (entity.isCrouching() ? Mth.PI / 12 : 0.0F), -3.3F,
                                                      3.3F));
                this.leftArm().setRotationY(this.head().yRot() - Mth.PI / 12);
            }
        }
        return shouldCancelFutureRepositions;
    }

    private boolean poseRightArm(T entity) {
        boolean shouldCancelFutureRepositions = false;
        switch (this.rightArmPose()) {
            case EMPTY -> this.rightArm().setRotationY(0.0F);
            case BLOCK -> {
                if (this.attackTime() == 0) {
                    this.rightArm().setRotationX(this.rightArm().xRot() * 0.5F + 0.942_477_9F);
                }
                this.rightArm().setRotationY(Mth.PI / 6.0F);
            }
            case ITEM -> {
                if (entity.isUsingItem()) {
                    if (this.shouldPoseArm(entity, HumanoidArm.RIGHT)) {
                        ItemStack stack = entity.getUseItem();
                        Item useItem = stack.getItem();
                        UseAnim action = useItem.getUseAnimation(stack);
                        if (action == UseAnim.EAT || action == UseAnim.DRINK) {
                            this.rightArm().setRotationX(Mth.lerp(-1.0f * (entity.getXRot() - 90.0f) / 180.0f, 1.0f, 2.0f) -
                                                         Mth.sin(entity.tickCount * 1.5f) * 0.1f);
                            this.rightArm().setRotationY(0.3f);
                            this.rightArm().setRotationZ(0.3f);
                            return true;
                        }
                    }
                }
                if (this.attackTime() == 0) {
                    this.rightArm().setRotationX(this.rightArm().xRot() * 0.5F + Mth.PI / 10.0F);
                }
                this.rightArm().setRotationY(0.0F);
            }
            case THROW_SPEAR -> {
                if (this.swimAmount() > 0 && entity.isInWater()) {
                    this.rightArm().setRotationX(this.rightArm().xRot() * 0.5F + Mth.PI);
                }
                else {
                    this.rightArm().setRotationX(this.rightArm().xRot() * 0.5F + (Mth.PI - entity.getXRot() * Mth.DEG_TO_RAD));
                }
                this.rightArm().setRotationY(0.0F);
                shouldCancelFutureRepositions = true;
            }
            case BOW_AND_ARROW -> {
                this.rightArm().setRotationY(0.1F - this.head().yRot());
                this.leftArm().setRotationY(-0.1F - this.head().yRot() - 0.4F);
                this.rightArm().setRotationX(Mth.HALF_PI - this.head().xRot());
                this.leftArm().setRotationX(Mth.HALF_PI - this.head().xRot());
            }
            case CROSSBOW_CHARGE -> AnimationUtils.animateCrossbowCharge(this.rightArm(), this.leftArm(), entity, true);
            case CROSSBOW_HOLD -> AnimationUtils.animateCrossbowHold(this.rightArm(), this.leftArm(), this.head(), true);
            case SPYGLASS -> {
                this.rightArm()
                    .setRotationX(Mth.clamp(this.head().xRot() + 1.919_862_2F + (entity.isCrouching() ? 0.261_799_4F : 0.0F), -3.3F, 3.3F));
                this.rightArm().setRotationY(this.head().yRot() + 0.261_799_4F);
            }
        }
        return shouldCancelFutureRepositions;
    }

    @Override
    default void prepare(T entity, float limbSwing, float limbSwingAmount, float partialTicks) {
        this.setSwimAmount(entity.getSwimAmount(partialTicks));
        HMAgeableList.super.prepare(entity, limbSwing, limbSwingAmount, partialTicks);
    }

    HM rightArm();

    ArmPose rightArmPose();

    HM rightLeg();

    void setCrouching(boolean crouching);

    void setLeftArmPose(ArmPose leftArmPose);

    void setRightArmPose(ArmPose rightArmPose);

    void setSwimAmount(float swimAmount);

    @Override
    default void setup(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean isElytraFlying = entity.getFallFlyingTicks() > 4;
        boolean isVisuallySwimming = entity.isVisuallySwimming();
        this.head().setRotationY(netHeadYaw * Mth.DEG_TO_RAD);
        this.head().setRotationZ(0); //Reset model
        if (isElytraFlying) {
            this.head().setRotationX(-Mth.PI / 4.0F);
        }
        else if (this.swimAmount() > 0.0F) {
            if (isVisuallySwimming) {
                if (!entity.isInWater()) {
                    //Crawling pose
                    this.head().setRotationX(Mth.DEG_TO_RAD * (headPitch + 90));
                }
                else {
                    this.head().setRotationX(rotlerpRad(this.swimAmount(), this.head().xRot(), Mth.HALF_PI));
                }
                this.head().setRotationY(0);
                this.head().setRotationZ(Mth.DEG_TO_RAD * netHeadYaw);
            }
            else {
                //Return from swimming or crawling
                this.head().setRotationX(rotlerpRad(this.swimAmount(), this.head().xRot(), -Mth.DEG_TO_RAD * headPitch));
            }
        }
        else {
            this.head().setRotationX(Mth.DEG_TO_RAD * headPitch);
        }
        this.body().setRotationY(0.0F);
        this.rightArm().setPivotZ(0.0F);
        this.rightArm().setPivotX(5.0F);
        this.leftArm().setPivotZ(0.0F);
        this.leftArm().setPivotX(-5.0F);
        this.leftLeg().setPivotX(-1.9f);
        this.rightLeg().setPivotX(1.9f);
        float f = 1.0F;
        if (isElytraFlying) {
            f = (float) entity.getDeltaMovement().lengthSqr();
            f /= 0.2F;
            f = f * f * f;
        }
        if (f < 1.0F) {
            f = 1.0F;
        }
        this.rightArm().setRotationX(-Mth.cos(limbSwing * 0.666_2F + Mth.PI) * 2.0F * limbSwingAmount * 0.5F / f);
        this.leftArm().setRotationX(-Mth.cos(limbSwing * 0.666_2F) * 2.0F * limbSwingAmount * 0.5F / f);
        this.rightArm().setRotationZ(0);
        this.leftArm().setRotationZ(0);
        this.leftLeg().setRotationX(Mth.cos(limbSwing * 0.666_2F) * 1.4F * limbSwingAmount / f);
        this.rightLeg().setRotationX(Mth.cos(limbSwing * 0.666_2F + Mth.PI) * 1.4F * limbSwingAmount / f);
        this.rightLeg().setRotationY(0);
        this.leftLeg().setRotationY(0);
        this.rightLeg().setRotationZ(0);
        this.leftLeg().setRotationZ(0);
        if (this.riding()) {
            this.rightArm().addRotationX(Mth.PI / 5);
            this.leftArm().addRotationX(Mth.PI / 5);
            this.rightLeg().setRotationX(0.45F * Mth.PI);
            this.rightLeg().setRotationY(-Mth.PI / 10);
            this.rightLeg().setRotationZ(Mth.PI / 40);
            this.leftLeg().setRotationX(0.45F * Mth.PI);
            this.leftLeg().setRotationY(Mth.PI / 10);
            this.leftLeg().setRotationZ(-Mth.PI / 40);
        }
        this.rightArm().setRotationY(0);
        this.leftArm().setRotationY(0);
        boolean isRightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        boolean isPoseTwoHanded = isRightHanded ? this.leftArmPose().isTwoHanded() : this.rightArmPose().isTwoHanded();
        boolean shouldCancelRight;
        boolean shouldCancelLeft;
        if (isRightHanded != isPoseTwoHanded) {
            shouldCancelLeft = this.poseLeftArm(entity);
            shouldCancelRight = this.poseRightArm(entity);
        }
        else {
            shouldCancelRight = this.poseRightArm(entity);
            shouldCancelLeft = this.poseLeftArm(entity);
        }
        this.setupAttackAnim(entity, ageInTicks);
        if (this.crouching()) {
            this.body().setRotationX(-0.5F);
            if (!shouldCancelRight) {
                this.rightArm().addRotationX(-0.4F);
            }
            if (!shouldCancelLeft) {
                this.leftArm().addRotationX(-0.4F);
            }
            this.rightLeg().setPivotZ(4.0F);
            this.leftLeg().setPivotZ(4.0F);
            this.rightLeg().setPivotY(11.8F);
            this.leftLeg().setPivotY(11.8F);
            this.head().setPivotY(19.8F);
            this.body().setPivotY(20.8F);
            this.leftArm().setPivotY(18.8F);
            this.rightArm().setPivotY(18.8F);
        }
        else {
            this.body().setRotationX(0.0F);
            this.rightLeg().setPivotZ(0.1F);
            this.leftLeg().setPivotZ(0.1F);
            this.rightLeg().setPivotY(12.0F);
            this.leftLeg().setPivotY(12.0F);
            this.head().setPivotY(24.0F);
            this.body().setPivotY(24.0F);
            this.leftArm().setPivotY(22.0F);
            this.rightArm().setPivotY(22.0F);
        }
        if (this.rightArmPose() != ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.rightArm(), ageInTicks, 1.0F);
        }
        if (this.leftArmPose() != ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.leftArm(), ageInTicks, -1.0F);
        }
        if (this.swimAmount() > 0.0F) {
            float f1 = limbSwing % 26.0F;
            HumanoidArm attackArm = this.getAttackArm(entity);
            float rightArmAnim = attackArm == HumanoidArm.RIGHT && this.attackTime() > 0.0F ? 0.0F : this.swimAmount();
            float leftArmAnim = attackArm == HumanoidArm.LEFT && this.attackTime() > 0.0F ? 0.0F : this.swimAmount();
            if (f1 < 14.0F) {
                if (!shouldCancelRight) {
                    this.rightArm().setRotationX(Mth.lerp(rightArmAnim, this.rightArm().xRot(), 0.0F));
                    this.rightArm().setRotationY(Mth.lerp(rightArmAnim, this.rightArm().yRot(), -Mth.PI));
                    this.rightArm()
                        .setRotationZ(Mth.lerp(rightArmAnim, this.rightArm().zRot(),
                                               Mth.PI - 1.870_796_4F * quadraticArmUpdate(f1) / quadraticArmUpdate(14.0F)));
                }
                else {
                    this.rightArm().addRotationX(Mth.HALF_PI);
                }
                if (!shouldCancelLeft) {
                    this.leftArm().setRotationX(rotlerpRad(leftArmAnim, this.leftArm().xRot(), 0.0F));
                    this.leftArm().setRotationY(Mth.lerp(leftArmAnim, this.leftArm().yRot(), Mth.PI));
                    this.leftArm()
                        .setRotationZ(rotlerpRad(leftArmAnim, this.leftArm().zRot(),
                                                 Mth.PI +
                                                 1.870_796_4F * quadraticArmUpdate(f1) / quadraticArmUpdate(14.0F)));
                }
                else {
                    this.leftArm().addRotationX(Mth.HALF_PI);
                }
            }
            else if (f1 >= 14.0F && f1 < 22.0F) {
                float f6 = (f1 - 14.0F) / 8.0F;
                if (!shouldCancelRight) {
                    this.rightArm().setRotationX(-Mth.lerp(rightArmAnim, -this.rightArm().xRot(), Mth.HALF_PI * f6));
                    this.rightArm().setRotationY(Mth.lerp(rightArmAnim, this.rightArm().yRot(), -Mth.PI));
                    this.rightArm().setRotationZ(Mth.lerp(rightArmAnim, this.rightArm().zRot(), 1.270_796_3F + 1.870_796_4F * f6));
                }
                else {
                    this.rightArm().addRotationX(Mth.HALF_PI);
                }
                if (!shouldCancelLeft) {
                    this.leftArm().setRotationX(rotlerpRad(leftArmAnim, this.leftArm().xRot(), -Mth.HALF_PI * f6));
                    this.leftArm().setRotationY(Mth.lerp(leftArmAnim, this.leftArm().yRot(), Mth.PI));
                    this.leftArm().setRotationZ(rotlerpRad(leftArmAnim, this.leftArm().zRot(), 5.012_389F - 1.870_796_4F * f6));
                }
                else {
                    this.leftArm().addRotationX(Mth.HALF_PI);
                }
            }
            else if (f1 >= 22.0F && f1 < 26.0F) {
                float f4 = (f1 - 22.0F) / 4.0F;
                if (!shouldCancelRight) {
                    this.rightArm().setRotationX(-Mth.lerp(rightArmAnim, -this.rightArm().xRot(), Mth.HALF_PI - Mth.HALF_PI * f4));
                    this.rightArm().setRotationY(Mth.lerp(rightArmAnim, this.rightArm().yRot(), -Mth.PI));
                    this.rightArm().setRotationZ(Mth.lerp(rightArmAnim, this.rightArm().zRot(), Mth.PI));
                }
                else {
                    this.rightArm().addRotationX(Mth.HALF_PI);
                }
                if (!shouldCancelLeft) {
                    this.leftArm().setRotationX(-rotlerpRad(leftArmAnim, -this.leftArm().xRot(), Mth.HALF_PI - Mth.HALF_PI * f4));
                    this.leftArm().setRotationY(Mth.lerp(leftArmAnim, this.leftArm().yRot(), Mth.PI));
                    this.leftArm().setRotationZ(rotlerpRad(leftArmAnim, this.leftArm().zRot(), Mth.PI));
                }
                else {
                    this.leftArm().addRotationX(Mth.HALF_PI);
                }
            }
            this.leftLeg().setRotationX(Mth.lerp(this.swimAmount(), this.leftLeg().xRot(), -0.3F * Mth.cos(limbSwing / 3 + Mth.PI)));
            this.rightLeg().setRotationX(Mth.lerp(this.swimAmount(), this.rightLeg().xRot(), -0.3F * Mth.cos(limbSwing / 3)));
        }
        this.hat().copy(this.head());
    }

    default void setupAttackAnim(T entity, float ageInTicks) {
        if (this.attackTime() > 0.0F) {
            HumanoidArm attackingSide = this.getAttackArm(entity);
            if (!(((ILivingEntityPatch) entity).shouldRenderSpecialAttack() &&
                  ((ILivingEntityPatch) entity).getSpecialAttackType() != IMelee.BARE_HAND_ATTACK &&
                  entity.getMainArm() == this.getAttackArm(entity))) {
                HM attackingArm = this.arm(attackingSide);
                float attackTime = this.attackTime();
                this.body().setRotationY(-Mth.sin(MathHelper.sqrt(attackTime) * Mth.TWO_PI) * 0.2F);
                if (attackingSide == HumanoidArm.LEFT) {
                    this.body().invertRotationY();
                }
                this.rightArm().setPivotZ(Mth.sin(this.body().yRot()) * 5.0F);
                this.rightArm().setPivotX(Mth.cos(this.body().yRot()) * 5.0F);
                this.leftArm().setPivotZ(-Mth.sin(this.body().yRot()) * 5.0F);
                this.leftArm().setPivotX(-Mth.cos(this.body().yRot()) * 5.0F);
                this.rightArm().addRotationY(this.body().yRot());
                this.leftArm().addRotationY(this.body().yRot());
                this.leftArm().addRotationX(this.body().yRot());
                attackTime = 1.0F - this.attackTime();
                attackTime *= attackTime;
                attackTime *= attackTime;
                attackTime = 1.0F - attackTime;
                float f1 = Mth.sin(attackTime * Mth.PI);
                float f2 = Mth.sin(this.attackTime() * Mth.PI) * -(-this.head().xRot() - 0.7F) * 0.75F;
                attackingArm.addRotationX(f1 * 1.2f + f2);
                attackingArm.addRotationY(this.body().yRot() * 2.0F);
                attackingArm.addRotationZ(Mth.sin(this.attackTime() * Mth.PI) * -0.4F);
            }
        }
        float partialTicks = Minecraft.getInstance().getFrameTime();
        if (((ILivingEntityPatch) entity).shouldRenderSpecialAttack()) {
            IMelee.IAttackType type = ((ILivingEntityPatch) entity).getSpecialAttackType();
            if (type instanceof IMelee.BasicAttackType basic) {
                switch (basic) {
                    case AXE_SWEEP -> {
                        HumanoidArm attackingSide = entity.getMainArm();
                        HM attackingArm = this.arm(attackingSide);
                        float progress = ((ILivingEntityPatch) entity).getSpecialAttackProgress(partialTicks);
                        attackingArm.setRotationX(progress * 2.25f);
                        attackingArm.setRotationY(this.head().xRot() * 2 / 3);
                        attackingArm.setRotationZ(Mth.PI / 6 + Mth.PI / 3 * progress);
                    }
                    case SPEAR_STAB -> {
                        HumanoidArm attackingSide = entity.getMainArm();
                        HM attackingArm = this.arm(attackingSide);
                        float progress = ((ILivingEntityPatch) entity).getSpecialAttackProgress(partialTicks);
                        if (progress < 0.5f) {
                            attackingArm.setRotationX(MathHelper.lerpRad(progress * 2, Mth.PI / 10, -Mth.PI / 4, false));
                        }
                        else if (progress < 0.75f) {
                            progress -= 0.5f;
                            attackingArm.setRotationX(MathHelper.lerpRad(progress * 4, -Mth.PI / 4, this.head().xRot() * 2 / 3 + Mth.HALF_PI, false));
                            attackingArm.setRotationY(MathHelper.lerpRad(progress * 4, 0, Mth.PI / 20, false));
                        }
                        else {
                            attackingArm.setRotationX(this.head().xRot() * 2 / 3 + Mth.HALF_PI);
                            attackingArm.setRotationY(Mth.PI / 20);
                        }
                    }
                }
            }
        }
    }

    private boolean shouldPoseArm(T entity, HumanoidArm side) {
        if (entity.getMainArm() == side) {
            return entity.getUsedItemHand() == InteractionHand.MAIN_HAND;
        }
        return entity.getUsedItemHand() == InteractionHand.OFF_HAND;
    }

    float swimAmount();
}
