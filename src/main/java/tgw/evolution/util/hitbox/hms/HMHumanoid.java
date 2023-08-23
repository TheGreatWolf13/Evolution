package tgw.evolution.util.hitbox.hms;

import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import tgw.evolution.EvolutionClient;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.PatchLivingEntity;
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
        return side == HumanoidArm.LEFT ? this.armL() : this.armR();
    }

    HM armL();

    HM armR();

    HM body();

    boolean crouching();

    default HM forearm(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? this.forearmL() : this.forearmR();
    }

    HM forearmL();

    HM forearmR();

    HM forelegL();

    HM forelegR();

    private HumanoidArm getSwingingArm(T entity) {
        HumanoidArm arm = entity.getMainArm();
        return entity.swingingArm == InteractionHand.MAIN_HAND ? arm : arm.getOpposite();
    }

    HM hat();

    HM head();

    default HM item(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? this.itemR() : this.itemL();
    }

    HM itemL();

    HM itemR();

    ArmPose leftArmPose();

    HM legL();

    HM legR();

    private void poseLeftArm(T entity) {
        HM armL = this.armL();
        HM itemL = this.itemL();
        switch (this.leftArmPose()) {
            case EMPTY -> armL.setRotationY(0.0F);
            case BLOCK -> {
                armL.setRotationX(armL.xRot() * 0.5F + 0.3f * Mth.PI);
                armL.setRotationY(-Mth.PI / 6.0F);
            }
            case ITEM -> {
                if (entity.isUsingItem()) {
                    if (this.shouldPoseArm(entity, HumanoidArm.LEFT)) {
                        ItemStack stack = entity.getUseItem();
                        Item useItem = stack.getItem();
                        UseAnim action = useItem.getUseAnimation(stack);
                        if (action == UseAnim.EAT || action == UseAnim.DRINK) {
                            armL.setRotationX((90 - entity.getXRot()) * Mth.DEG_TO_RAD - 10 * Mth.DEG_TO_RAD + Mth.sin(
                                    (entity.tickCount + EvolutionClient.getPartialTicks()) * 1.5f) * 3 * Mth.DEG_TO_RAD);
                            armL.setRotationY(-0.3f);
                            armL.setRotationZ(-0.3f);
                            this.setShouldCancelLeft(true);
                            return;
                        }
                    }
                }
                armL.setRotationX(armL.xRot() * 0.5F + Mth.PI / 10.0F);
                armL.setRotationY(0.0F);
            }
            case THROW_SPEAR -> {
                if (this.swimAmount() > 0 && entity.isInWater()) {
                    armL.setRotationX(armL.xRot() * 0.5F + Mth.PI);
                }
                else {
                    armL.setRotationX(armL.xRot() * 0.5F + (Mth.PI - entity.getXRot() * Mth.DEG_TO_RAD));
                }
                armL.setRotationY(0.0F);
                itemL.setRotationX(180 * Mth.DEG_TO_RAD);
            }
//            case BOW_AND_ARROW -> {
//                this.armR().setRotationY(0.1F - this.head().yRot() + 0.4F);
//                this.armL().setRotationY(-0.1F - this.head().yRot());
//                this.armR().setRotationX(Mth.HALF_PI - this.head().xRot());
//                this.armL().setRotationX(Mth.HALF_PI - this.head().xRot());
//            }
//            case CROSSBOW_CHARGE -> AnimationUtils.animateCrossbowCharge(this.armR(), this.armL(), entity, false);
//            case CROSSBOW_HOLD -> AnimationUtils.animateCrossbowHold(this.armR(), this.armL(), this.head(), false);
//            case SPYGLASS -> {
//                this.armL().setRotationX(Mth.clamp(this.head().xRot() + 1.919_862_2F + (entity.isCrouching() ? Mth.PI / 12 : 0.0F), -3.3F,
//                                                   3.3F));
//                this.armL().setRotationY(this.head().yRot() - Mth.PI / 12);
//            }
        }
    }

    private void poseRightArm(T entity) {
        HM armR = this.armR();
        HM itemR = this.itemR();
        switch (this.rightArmPose()) {
            case EMPTY -> armR.setRotationY(0.0F);
            case BLOCK -> {
                if (this.attackTime() == 0) {
                    armR.setRotationX(armR.xRot() * 0.5F + 0.942_477_9F);
                }
                armR.setRotationY(Mth.PI / 6.0F);
            }
            case ITEM -> {
                if (entity.isUsingItem()) {
                    if (this.shouldPoseArm(entity, HumanoidArm.RIGHT)) {
                        ItemStack stack = entity.getUseItem();
                        Item useItem = stack.getItem();
                        UseAnim action = useItem.getUseAnimation(stack);
                        if (action == UseAnim.EAT || action == UseAnim.DRINK) {
                            armR.setRotationX((90 - entity.getXRot()) * Mth.DEG_TO_RAD - 10 * Mth.DEG_TO_RAD + Mth.sin(
                                    (entity.tickCount + EvolutionClient.getPartialTicks()) * 1.5f) * 3 * Mth.DEG_TO_RAD);
                            armR.setRotationY(0.3f);
                            armR.setRotationZ(0.3f);
                            this.setShouldCancelRight(true);
                            return;
                        }
                    }
                }
                if (this.attackTime() == 0) {
                    armR.setRotationX(armR.xRot() * 0.5F + Mth.PI / 10.0F);
                }
                armR.setRotationY(0.0F);
            }
            case THROW_SPEAR -> {
                if (this.swimAmount() > 0 && entity.isInWater()) {
                    armR.setRotationX(armR.xRot() * 0.5F + Mth.PI);
                }
                else {
                    armR.setRotationX(armR.xRot() * 0.5F + (Mth.PI - entity.getXRot() * Mth.DEG_TO_RAD));
                }
                armR.setRotationY(0.0F);
                itemR.setRotationX(180 * Mth.DEG_TO_RAD);
            }
//            case BOW_AND_ARROW -> {
//                this.armR().setRotationY(0.1F - this.head().yRot());
//                this.armL().setRotationY(-0.1F - this.head().yRot() - 0.4F);
//                this.armR().setRotationX(Mth.HALF_PI - this.head().xRot());
//                this.armL().setRotationX(Mth.HALF_PI - this.head().xRot());
//            }
//            case CROSSBOW_CHARGE -> AnimationUtils.animateCrossbowCharge(this.armR(), this.armL(), entity, true);
//            case CROSSBOW_HOLD -> AnimationUtils.animateCrossbowHold(this.armR(), this.armL(), this.head(), true);
//            case SPYGLASS -> {
//                this.armR()
//                    .setRotationX(Mth.clamp(this.head().xRot() + 1.919_862_2F + (entity.isCrouching() ? 0.261_799_4F : 0.0F), -3.3F, 3.3F));
//                this.armR().setRotationY(this.head().yRot() + 0.261_799_4F);
//            }
        }
    }

    @Override
    default void prepare(T entity, float limbSwing, float limbSwingAmount, float partialTicks) {
        this.setSwimAmount(entity.getSwimAmount(partialTicks));
        HMAgeableList.super.prepare(entity, limbSwing, limbSwingAmount, partialTicks);
    }

    ArmPose rightArmPose();

    void setCrouching(boolean crouching);

    void setLeftArmPose(ArmPose leftArmPose);

    void setRightArmPose(ArmPose rightArmPose);

    void setShouldCancelLeft(boolean shouldCancel);

    void setShouldCancelRight(boolean shouldCancel);

    void setSwimAmount(float swimAmount);

    @Override
    default void setup(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        //TODO fix other mobs
        if (!(entity instanceof Player)) {
            return;
        }
        HM head = this.head();
        HM body = this.body();
        HM armR = this.armR();
        HM forearmR = this.forearmR();
        HM armL = this.armL();
        HM forearmL = this.forearmL();
        HM legR = this.legR();
        HM forelegR = this.forelegR();
        HM legL = this.legL();
        HM forelegL = this.forelegL();
        HM itemR = this.itemR();
        HM itemL = this.itemL();
        float swimAmount = this.swimAmount();
        boolean inWater = entity.isInWater();
        this.setShouldCancelLeft(false);
        this.setShouldCancelRight(false);
        //Reset model
        //  Pivots
        head.setPivotY(24);
        body.setPivotY(24);
        legR.setPivotY(-12);
        legR.setPivotZ(0);
        legL.setPivotY(-12);
        legL.setPivotZ(0);
        itemR.setPivot(0, 0, 0);
        itemL.setPivot(0, 0, 0);
        //  Rotation
        headPitch *= Mth.DEG_TO_RAD;
        head.setRotationX(headPitch);
        head.setRotationY(Mth.DEG_TO_RAD * netHeadYaw);
        body.setRotation(0, 0, 0);
        armR.setRotation(0, 0, 0);
        forearmR.setRotation(0, 0, 0);
        armL.setRotation(0, 0, 0);
        forearmL.setRotation(0, 0, 0);
        legR.setRotation(0, 0, 0);
        forelegR.setRotationX(0);
        legL.setRotation(0, 0, 0);
        forelegL.setRotationX(0);
        itemR.setRotation(0, 0, 0);
        itemL.setRotation(0, 0, 0);
        //Setup poses
        boolean crouching = this.crouching();
        boolean crawling = !inWater && (swimAmount > 0 || entity.getPose() == Pose.SWIMMING);
        boolean swimming = inWater && swimAmount > 0;
        boolean shouldWalk = false;
        if (this.riding()) {
            armR.setRotationX(18 * Mth.DEG_TO_RAD);
            forearmR.setRotationX(18 * Mth.DEG_TO_RAD);
            armL.setRotationX(18 * Mth.DEG_TO_RAD);
            forearmL.setRotationX(18 * Mth.DEG_TO_RAD);
            legR.setRotationX(81 * Mth.DEG_TO_RAD);
            legR.setRotationY(-18 * Mth.DEG_TO_RAD);
            forelegR.setRotationX(-70 * Mth.DEG_TO_RAD);
            legL.setRotationX(81 * Mth.DEG_TO_RAD);
            legL.setRotationY(18 * Mth.DEG_TO_RAD);
            forelegL.setRotationX(-70 * Mth.DEG_TO_RAD);
        }
        else if (crawling) {
            if (swimAmount < 1) {
                //Transition
                float headOffset;
                float bodyOffset;
                float legOffset;
                float bodyRot0;
                float legRot0;
                float foreLegRot0;
                if (entity.getLastPose() == Pose.CROUCHING || entity.getPose() == Pose.CROUCHING) {
                    headOffset = -7;
                    bodyOffset = -6;
                    legOffset = 1;
                    bodyRot0 = -30 * Mth.DEG_TO_RAD;
                    legRot0 = 75 * Mth.DEG_TO_RAD;
                    foreLegRot0 = -90 * Mth.DEG_TO_RAD;
                }
                else {
                    headOffset = 0;
                    bodyOffset = 0;
                    legOffset = 0;
                    bodyRot0 = 0;
                    legRot0 = 0;
                    foreLegRot0 = 0;
                }
                if (swimAmount < 0.25) {
                    float t = AnimationUtils.normParameter(swimAmount, 0, 0.25f);
                    float quadIn = AnimationUtils.easingQuadraticIn(t);
                    float offset = -5 * quadIn;
                    float antiT = 1 - t;
                    head.translateY(offset + headOffset);
                    body.translateY(offset + bodyOffset);
                    body.setRotationX(bodyRot0);
                    legR.translateY(legOffset * antiT);
                    legR.addRotationX(legRot0 * antiT - 35 * Mth.DEG_TO_RAD * t);
                    forelegR.addRotationX(foreLegRot0 * antiT - 55 * Mth.DEG_TO_RAD * t);
                    legL.translateY(legOffset * antiT);
                    legL.addRotationX(legRot0 * antiT + 80 * Mth.DEG_TO_RAD * t);
                    forelegL.addRotationX(foreLegRot0 * antiT - 80 * Mth.DEG_TO_RAD * t);
                }
                else if (swimAmount < 0.5) {
                    float t = AnimationUtils.normParameter(swimAmount, 0.25f, 0.5f);
                    float quadOut = AnimationUtils.easingQuadraticOut(t);
                    float offset = -5 + 1 * quadOut;
                    head.translateY(offset + headOffset);
                    body.translateY(offset + bodyOffset);
                    body.setRotationX(bodyRot0);
                    legR.addRotationX(-35 * Mth.DEG_TO_RAD + 35 * Mth.DEG_TO_RAD * t);
                    forelegR.addRotationX(-55 * Mth.DEG_TO_RAD + (-90 * Mth.DEG_TO_RAD + 55 * Mth.DEG_TO_RAD) * t);
                    legL.addRotationX(80 * Mth.DEG_TO_RAD - 80 * Mth.DEG_TO_RAD * t);
                    forelegL.addRotationX(-80 * Mth.DEG_TO_RAD + (-90 * Mth.DEG_TO_RAD + 80 * Mth.DEG_TO_RAD) * t);
                }
                else {
                    float t = AnimationUtils.normParameter(swimAmount, 0.5f, 1.0f);
                    float sineIn = AnimationUtils.easingSineIn(t);
                    float offset1795 = -4 + (-17.95f + 4) * sineIn;
                    float offset18 = -4 + (-18 + 4) * sineIn;
                    float offset2 = 1.99f * sineIn;
                    float rot90 = 90 * Mth.DEG_TO_RAD * t;
                    float antiT = 1 - t;
                    head.translateY(offset1795 + headOffset * antiT);
                    body.translateY(offset18 + bodyOffset * antiT);
                    body.setRotationX(bodyRot0 - (90 * Mth.DEG_TO_RAD + bodyRot0) * t);
                    armR.addRotationX(rot90);
                    forearmR.addRotationX(rot90);
                    armL.addRotationX(rot90);
                    forearmL.addRotationX(rot90);
                    legR.translateZ(offset2);
                    legR.addRotationX(rot90);
                    forelegR.addRotationX(-90 * Mth.DEG_TO_RAD);
                    legL.translateZ(offset2);
                    legL.addRotationX(rot90);
                    forelegL.addRotationX(-90 * Mth.DEG_TO_RAD);
                }
            }
            else {
                //Crawling Animation
                float sinLS = Mth.sin(limbSwing);
                float rot90 = limbSwingAmount * sinLS;
                float halfRot90 = 0.5f * limbSwingAmount * sinLS;
                head.translateY(-17.95f);
                body.translateY(-18);
                body.addRotationX(-90 * Mth.DEG_TO_RAD);
                armR.addRotationX(-rot90 + 90 * Mth.DEG_TO_RAD);
                forearmR.addRotationX(rot90 + 90 * Mth.DEG_TO_RAD);
                armL.addRotationX(rot90 + 90 * Mth.DEG_TO_RAD);
                forearmL.addRotationX(-rot90 + 90 * Mth.DEG_TO_RAD);
                legR.translateZ(1.99f);
                legR.addRotationX(halfRot90 + 90 * Mth.DEG_TO_RAD);
                forelegR.addRotationX(-halfRot90 - 90 * Mth.DEG_TO_RAD);
                legL.translateZ(1.99f);
                legL.addRotationX(-halfRot90 + 90 * Mth.DEG_TO_RAD);
                forelegL.addRotationX(halfRot90 - 90 * Mth.DEG_TO_RAD);
            }
        }
        else if (crouching) {
            shouldWalk = true;
            head.setPivotY(17);
            body.setPivotY(18);
            body.setRotationX(-30 * Mth.DEG_TO_RAD);
            if (!this.shouldCancelRight()) {
                armR.setRotationX(15 * Mth.DEG_TO_RAD);
                forearmR.setRotationX(15 * Mth.DEG_TO_RAD);
            }
            if (!this.shouldCancelLeft()) {
                armL.setRotationX(15 * Mth.DEG_TO_RAD);
                forearmL.setRotationX(15 * Mth.DEG_TO_RAD);
            }
            legR.setPivotY(-11);
            legR.setRotationX(75 * Mth.DEG_TO_RAD);
            forelegR.setRotationX(-90 * Mth.DEG_TO_RAD);
            legL.setPivotY(-11);
            legL.setRotationX(75 * Mth.DEG_TO_RAD);
            forelegL.setRotationX(-90 * Mth.DEG_TO_RAD);
        }
        else if (swimming) {
            //Transition
            if (swimAmount < 1) {
                if (swimAmount < 0.5) {
                    float t = AnimationUtils.normParameter(swimAmount, 0, 0.5f);
                    float offset = -19 * t;
                    head.translateY(offset);
                    body.translateY(offset);
                }
                else {
                    float t = AnimationUtils.normParameter(swimAmount, 0.5f, 1);
                    head.translateY(-19);
                    body.translateY(-19);
                    body.setRotationX((headPitch - Mth.HALF_PI) * t);
                }
            }
            else {
                //Swimming Animation
                float anim = Math.abs(limbSwing) / (4 * Mth.PI);
                head.translateY(-19);
                body.translateY(-19);
                body.setRotationX(headPitch - Mth.HALF_PI);
                //      Fully submerged
                if (entity.isFullySubmerged(FluidTags.WATER)) {
                    armR.setRotationY(180 * Mth.DEG_TO_RAD);
                    armL.setRotationY(180 * Mth.DEG_TO_RAD);
                    if (anim < 0.55) {
                        float t = AnimationUtils.normParameter(anim, 0, 0.55f);
                        float armZ = 180 * Mth.DEG_TO_RAD - 150 * Mth.DEG_TO_RAD * AnimationUtils.easingQuadraticOut(t);
                        float legZ = 15 * Mth.DEG_TO_RAD - 15 * Mth.DEG_TO_RAD * AnimationUtils.easingCubicOut(t);
                        armR.setRotationZ(armZ);
                        armL.setRotationZ(-armZ);
                        legR.setRotationZ(legZ);
                        legL.setRotationZ(-legZ);
                    }
                    else if (anim < 0.85) {
                        float t = AnimationUtils.normParameter(anim, 0.55f, 0.85f);
                        float armX = -90 * Mth.DEG_TO_RAD * t;
                        float armZ = 30 * Mth.DEG_TO_RAD + 150 * Mth.DEG_TO_RAD * t;
                        float forearmZ = -60 * Mth.DEG_TO_RAD * AnimationUtils.easingQuadraticOut(t);
                        float sineIn = AnimationUtils.easingSineIn(t);
                        float legX = 60 * Mth.DEG_TO_RAD * sineIn;
                        float forelegX = -120 * Mth.DEG_TO_RAD * sineIn;
                        armR.setRotationX(armX);
                        armR.setRotationZ(armZ);
                        forearmR.setRotationZ(forearmZ);
                        armL.setRotationX(armX);
                        armL.setRotationZ(-armZ);
                        forearmL.setRotationZ(-forearmZ);
                        legR.setRotationX(legX);
                        forelegR.setRotationX(forelegX);
                        legL.setRotationX(legX);
                        forelegL.setRotationX(forelegX);
                    }
                    else {
                        float t = AnimationUtils.normParameter(anim, 0.85f, 1);
                        float armX = -90 * Mth.DEG_TO_RAD + 90 * Mth.DEG_TO_RAD * t;
                        float forearmZ = -60 * Mth.DEG_TO_RAD + 60 * Mth.DEG_TO_RAD * AnimationUtils.easingQuadraticOut(t);
                        float cubicOut = AnimationUtils.easingCubicOut(t);
                        float legX = 60 * Mth.DEG_TO_RAD - 60 * Mth.DEG_TO_RAD * cubicOut;
                        float legZ = 15 * Mth.DEG_TO_RAD * cubicOut;
                        float forelegX = -120 * Mth.DEG_TO_RAD + 120 * Mth.DEG_TO_RAD * AnimationUtils.easingSineOut(t);
                        armR.setRotationX(armX);
                        armR.setRotationZ(180 * Mth.DEG_TO_RAD);
                        forearmR.setRotationZ(forearmZ);
                        armL.setRotationX(armX);
                        armL.setRotationZ(180 * Mth.DEG_TO_RAD);
                        forearmL.setRotationZ(-forearmZ);
                        legR.setRotationX(legX);
                        legR.setRotationZ(legZ);
                        forelegR.setRotationX(forelegX);
                        legL.setRotationX(legX);
                        legL.setRotationZ(-legZ);
                        forelegL.setRotationX(forelegX);
                    }
                }
                //      Surface
                else {
                    if (anim < 0.25) {
                        float t = AnimationUtils.normParameter(anim, 0, 0.25f);
                        float rot28 = 28 * Mth.DEG_TO_RAD * t;
                        float rot90 = 90 * Mth.DEG_TO_RAD * t;
                        body.setRotationY(rot28 * Mth.sin(headPitch));
                        body.setRotationZ(-rot28 * Mth.cos(headPitch));
                        armR.setRotationX(180 * Mth.DEG_TO_RAD - rot90);
                        armR.setRotationY(-rot28);
                        armL.setRotationX(-rot90);
                        armL.setRotationY(-28 * 1.5f * Mth.DEG_TO_RAD * t);
                        forearmL.setRotationX(-rot90);
                        legR.setRotationY(-rot28);
                        forelegR.setRotationX(-rot28);
                        legL.setRotationX(rot28);
                        legL.setRotationY(-rot28);
                    }
                    else if (anim < 0.5) {
                        float t = AnimationUtils.normParameter(anim, 0.25f, 0.5f);
                        float rot28 = 28 * Mth.DEG_TO_RAD * t;
                        float rot90 = 90 * Mth.DEG_TO_RAD * t;
                        body.setRotationY((28 * Mth.DEG_TO_RAD - rot28) * Mth.sin(headPitch));
                        body.setRotationZ((-28 * Mth.DEG_TO_RAD + rot28) * Mth.cos(headPitch));
                        armR.setRotationX(90 * Mth.DEG_TO_RAD - rot90);
                        armR.setRotationY(-28 * Mth.DEG_TO_RAD + rot28);
                        armL.setRotationX(-90 * Mth.DEG_TO_RAD - rot90);
                        armL.setRotationY(-28 * 1.5f * Mth.DEG_TO_RAD + 28 * 1.5f * Mth.DEG_TO_RAD * t);
                        forearmL.setRotationX(-90 * Mth.DEG_TO_RAD + rot90);
                        legR.setRotationY(-28 * Mth.DEG_TO_RAD + rot28);
                        forelegR.setRotationX(-28 * Mth.DEG_TO_RAD + rot28);
                        legL.setRotationX(28 * Mth.DEG_TO_RAD - rot28);
                        legL.setRotationY(-28 * Mth.DEG_TO_RAD + rot28);
                    }
                    else if (anim < 0.75) {
                        float t = AnimationUtils.normParameter(anim, 0.5f, 0.75f);
                        float rot28 = 28 * Mth.DEG_TO_RAD * t;
                        float rot90 = 90 * Mth.DEG_TO_RAD * t;
                        body.setRotationY(-rot28 * Mth.sin(headPitch));
                        body.setRotationZ(rot28 * Mth.cos(headPitch));
                        armR.setRotationX(-rot90);
                        armR.setRotationY(28 * 1.5f * Mth.DEG_TO_RAD * t);
                        forearmR.setRotationX(-rot90);
                        armL.setRotationX(-180 * Mth.DEG_TO_RAD - rot90);
                        armL.setRotationY(rot28);
                        legR.setRotationX(rot28);
                        legR.setRotationY(rot28);
                        legL.setRotationY(rot28);
                        forelegL.setRotationX(-rot28);
                    }
                    else {
                        float t = AnimationUtils.normParameter(anim, 0.75f, 1);
                        float rot28 = 28 * Mth.DEG_TO_RAD * t;
                        float rot90 = 90 * Mth.DEG_TO_RAD * t;
                        body.setRotationY((-28 * Mth.DEG_TO_RAD + rot28) * Mth.sin(headPitch));
                        body.setRotationZ((28 * Mth.DEG_TO_RAD - rot28) * Mth.cos(headPitch));
                        armR.setRotationX(-90 * Mth.DEG_TO_RAD - rot90);
                        armR.setRotationY(28 * 1.5f * Mth.DEG_TO_RAD - 28 * 1.5f * Mth.DEG_TO_RAD * t);
                        forearmR.setRotationX(-90 * Mth.DEG_TO_RAD + rot90);
                        armL.setRotationX(-270 * Mth.DEG_TO_RAD - rot90);
                        armL.setRotationY(28 * Mth.DEG_TO_RAD - rot28);
                        legR.setRotationX(28 * Mth.DEG_TO_RAD - rot28);
                        legR.setRotationY(28 * Mth.DEG_TO_RAD - rot28);
                        legL.setRotationY(28 * Mth.DEG_TO_RAD - rot28);
                        forelegL.setRotationX(-28 * Mth.DEG_TO_RAD + rot28);
                    }
                }
            }
        }
        else {
            //Standing
            shouldWalk = true;
        }
        if (shouldWalk) {
            //Walking animation
            float threeQuarterAmpl = 0.75f * limbSwingAmount;
            float fixedLimbSwing = 0.5f * limbSwing;
            float sinLS = Mth.sin(fixedLimbSwing);
            float cosLS = Mth.cos(fixedLimbSwing);
            armR.addRotationX(-threeQuarterAmpl * sinLS);
            armL.addRotationX(threeQuarterAmpl * sinLS);
            float halfAmpl = 0.5f * limbSwingAmount;
            forearmR.addRotationX(Math.max(0, -halfAmpl * sinLS));
            forearmL.addRotationX(Math.max(0, halfAmpl * sinLS));
            legR.addRotationX(halfAmpl * sinLS);
            legL.addRotationX(-halfAmpl * sinLS);
            forelegR.addRotationX(Math.min(0, -limbSwingAmount * cosLS));
            forelegL.addRotationX(Math.min(0, limbSwingAmount * cosLS));
            if (entity.isOnGround()) {
                float bodyMotion = limbSwingAmount * Mth.sin(limbSwing);
                head.translateY(bodyMotion);
                body.translateY(bodyMotion);
            }
        }
        //Setup hand poses
        AnimationUtils.setupItemPosition(itemR, HumanoidArm.RIGHT,
                                         entity.getMainArm() == HumanoidArm.RIGHT ? entity.getMainHandItem() : entity.getOffhandItem());
        AnimationUtils.setupItemPosition(itemL, HumanoidArm.LEFT,
                                         entity.getMainArm() == HumanoidArm.LEFT ? entity.getMainHandItem() : entity.getOffhandItem());
        if (!crawling && !swimming) {
            boolean isRightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
            boolean isPoseTwoHanded = isRightHanded ? this.leftArmPose().isTwoHanded() : this.rightArmPose().isTwoHanded();
            if (isRightHanded != isPoseTwoHanded) {
                this.poseLeftArm(entity);
                this.poseRightArm(entity);
            }
            else {
                this.poseRightArm(entity);
                this.poseLeftArm(entity);
            }
        }
        this.setupAttackAnim(entity, ageInTicks);
        if (swimAmount == 0) {
            if (this.rightArmPose() != ArmPose.SPYGLASS) {
                AnimationUtils.bobModelPart(armR, ageInTicks, 1.0F);
            }
            if (this.leftArmPose() != ArmPose.SPYGLASS) {
                AnimationUtils.bobModelPart(armL, ageInTicks, -1.0F);
            }
        }
        this.hat().copy(head);
    }

    default void setupAttackAnim(T entity, float ageInTicks) {
        PatchLivingEntity patch = (PatchLivingEntity) entity;
        HM body = this.body();
        HM armR = this.armR();
        HM armL = this.armL();
        HM legL = this.legL();
        HM legR = this.legR();
        if (this.attackTime() > 0.0F) {
            HumanoidArm attackingSide = this.getSwingingArm(entity);
            if (!(patch.shouldRenderSpecialAttack() && entity.getMainArm() == this.getSwingingArm(entity))) {
                HM attackingArm = this.arm(attackingSide);
                float attackTime = this.attackTime();
                body.setRotationY(-Mth.sin(MathHelper.sqrt(attackTime) * Mth.TWO_PI) * 0.2F);
                if (attackingSide == HumanoidArm.LEFT) {
                    body.invertRotationY();
                }
                armR.addRotationY(body.yRot());
                armL.addRotationX(body.yRot());
                legR.addRotationY(-body.yRot());
                legL.addRotationY(-body.yRot());
                attackTime = 1.0F - attackTime;
                attackTime *= attackTime;
                attackTime *= attackTime;
                attackTime = 1.0F - attackTime;
                float f1 = Mth.sin(attackTime * Mth.PI);
                float f2 = Mth.sin(this.attackTime() * Mth.PI) * -(-this.head().xRot() - 0.7F) * 0.75F;
                attackingArm.addRotationX(f1 * 1.2f + f2);
                attackingArm.addRotationY(body.yRot() * 2.0F);
                attackingArm.addRotationZ(Mth.sin(this.attackTime() * Mth.PI) * -0.4F);
            }
        }
        float partialTicks = EvolutionClient.getPartialTicks();
        if (patch.shouldRenderSpecialAttack()) {
            IMelee.IAttackType type = patch.getSpecialAttackType();
            if (type == IMelee.BARE_HAND_ATTACK) {
                if (entity.getOffhandItem().isEmpty()) {
                    int attackNumber = patch.getAttackNumber();
                    //First punch
                    if (attackNumber == 1) {
                        AnimationUtils.animatePunch(entity, patch.getSpecialAttackProgress(partialTicks), entity.getMainArm(), this.head(),
                                                    body, armR, armL, this.forearmR(), this.forearmL(), legR, legL,
                                                    false, true);
                    }
                    //Offhand punch
                    else if (attackNumber % 2 == 0) {
                        AnimationUtils.animatePunch(entity, patch.getSpecialAttackProgress(partialTicks), entity.getMainArm().getOpposite(),
                                                    this.head(), body, armR, armL, this.forearmR(), this.forearmL(), legR,
                                                    legL, true, true);
                    }
                    //Mainhand punch
                    else {
                        AnimationUtils.animatePunch(entity, patch.getSpecialAttackProgress(partialTicks), entity.getMainArm(), this.head(),
                                                    body, armR, armL, this.forearmR(), this.forearmL(), legR, legL,
                                                    true, true);
                    }
                }
                else {
                    AnimationUtils.animatePunch(entity, patch.getSpecialAttackProgress(partialTicks), entity.getMainArm(), this.head(), body,
                                                armR, armL, this.forearmR(), this.forearmL(), legR, legL, false, false);
                }
            }
            else if (type instanceof IMelee.BasicAttackType basic) {
                HumanoidArm attackingSide = entity.getMainArm();
                HM arm = this.arm(attackingSide);
                HM forearm = this.forearm(attackingSide);
                float progress = patch.getSpecialAttackProgress(partialTicks);
                int mult = attackingSide == HumanoidArm.RIGHT ? 1 : -1;
                switch (basic) {
                    case AXE_STRIKE_1 -> {
                        switch (patch.getAttackNumber()) {
                            case 1 -> AnimationUtils.strikeDown(progress, mult, body, legR, legL, arm, forearm);
                            case 2 -> AnimationUtils.strikeFromFarSide(progress, mult, body, legR, legL, arm, forearm);
                            case 3 -> AnimationUtils.strikeDown(progress, mult, body, legR, legL, arm, forearm, 40.804_9f * Mth.DEG_TO_RAD,
                                                                mult * 14.666_2f * Mth.DEG_TO_RAD, mult * 43.341_7f * Mth.DEG_TO_RAD);
                        }
                    }
                    case HOE_STRIKE_1 -> {
                        switch (patch.getAttackNumber()) {
                            case 1 -> AnimationUtils.strikeDown(progress, mult, body, legR, legL, arm, forearm);
                            case 2 -> AnimationUtils.strikeFromFarSide(progress, mult, body, legR, legL, arm, forearm);
                        }
                    }
                    case JAVELIN_THRUST -> AnimationUtils.thrust(progress, mult, body, legR, legL, arm, forearm, this.item(attackingSide),
                                                                 this.head().xRot());
                    case PICKAXE_STRIKE_1, SHOVEL_STRIKE_1 -> AnimationUtils.strikeDown(progress, mult, body, legR, legL, arm, forearm);
                }
            }
        }
    }

    boolean shouldCancelLeft();

    boolean shouldCancelRight();

    private boolean shouldPoseArm(T entity, HumanoidArm side) {
        if (entity.getMainArm() == side) {
            return entity.getUsedItemHand() == InteractionHand.MAIN_HAND;
        }
        return entity.getUsedItemHand() == InteractionHand.OFF_HAND;
    }

    float swimAmount();
}
