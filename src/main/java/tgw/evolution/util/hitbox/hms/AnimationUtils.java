package tgw.evolution.util.hitbox.hms;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.IModularTool;
import tgw.evolution.items.IEvolutionItem;
import tgw.evolution.items.modular.ItemModularTool;
import tgw.evolution.util.math.MathHelper;

public final class AnimationUtils {

    private AnimationUtils() {
    }

    public static void animateCrossbowCharge(HM rightArm, HM leftArm, LivingEntity entity, boolean rightHanded) {
        HM holdingArm = rightHanded ? rightArm : leftArm;
        HM pullingArm = rightHanded ? leftArm : rightArm;
        holdingArm.setRotationY(rightHanded ? -0.8F : 0.8F);
        holdingArm.setRotationX(-0.970_796_35F);
        pullingArm.setRotationX(holdingArm.xRot());
        float f = CrossbowItem.getChargeDuration(entity.getUseItem());
        float f1 = Mth.clamp(entity.getTicksUsingItem(), 0.0F, f);
        float f2 = f1 / f;
        pullingArm.setRotationY(Mth.lerp(f2, 0.4F, 0.85F) * (rightHanded ? 1 : -1));
        pullingArm.setRotationX(Mth.lerp(f2, pullingArm.xRot(), -Mth.PI / 2.0F));
    }

    public static void animateCrossbowHold(HM rightArm, HM leftArm, HM head, boolean rightHanded) {
        HM holdingArm = rightHanded ? rightArm : leftArm;
        HM pullingArm = rightHanded ? leftArm : rightArm;
        holdingArm.setRotationY((rightHanded ? -0.3F : 0.3F) + head.yRot());
        pullingArm.setRotationY((rightHanded ? 0.6F : -0.6F) + head.yRot());
        holdingArm.setRotationX(-Mth.PI / 2.0F + head.xRot() + 0.1F);
        pullingArm.setRotationX(-1.5F + head.xRot());
    }

    public static void animatePunch(LivingEntity entity,
                                    float progress,
                                    HumanoidArm attackingSide,
                                    HM head,
                                    HM body,
                                    HM armR,
                                    HM armL,
                                    HM forearmR,
                                    HM forearmL,
                                    HM legR,
                                    HM legL,
                                    boolean followUp, boolean both) {
        HM arm;
        HM opArm;
        HM forearm;
        HM opForearm;
        float mult;
        if (attackingSide == HumanoidArm.RIGHT) {
            arm = armR;
            opArm = armL;
            forearm = forearmR;
            opForearm = forearmL;
            mult = -1;
        }
        else {
            arm = armL;
            opArm = armR;
            forearm = forearmL;
            opForearm = forearmR;
            mult = 1;
        }
        if (progress < 0.5f) {
            float t = MathHelper.animInterval(progress, 0, 0.5f);
            float r;
            if (followUp) {
                r = mult * 30 * Mth.DEG_TO_RAD;
                arm.setRotationX(-20 * Mth.DEG_TO_RAD);
                forearm.setRotationX(130 * Mth.DEG_TO_RAD);
                if (both) {
                    opArm.setRotationX(-20 * Mth.DEG_TO_RAD);
                    opArm.setRotationY(-mult * 15 * Mth.DEG_TO_RAD);
                    opForearm.setRotationX(130 * Mth.DEG_TO_RAD);
                }
            }
            else {
                r = MathHelper.lerpRad(t, 0, mult * 30 * Mth.DEG_TO_RAD, false);
                float start = entity.getMainHandItem().isEmpty() ? 0 : Mth.PI / 10;
                float a = MathHelper.lerpRad(t, start, -20 * Mth.DEG_TO_RAD, false);
                float b = MathHelper.lerpRad(t, 0, 130 * Mth.DEG_TO_RAD, false);
                arm.setRotationX(a);
                forearm.setRotationX(b);
                if (both) {
                    opArm.setRotationX(a);
                    opForearm.setRotationX(b);
                }
            }
            body.setRotationY(r);
            legR.setRotationY(-r);
            legL.setRotationY(-r);
        }
        else if (progress < 5 / 6.0f) {
            float t = MathHelper.animInterval(progress, 0.5f, 5 / 6.0f);
            float r = MathHelper.lerpRad(t, mult * 30 * Mth.DEG_TO_RAD, mult * -30 * Mth.DEG_TO_RAD, true);
            body.setRotationY(r);
            legR.setRotationY(-r);
            legL.setRotationY(-r);
            arm.setRotationX(MathHelper.lerpRad(t, -20 * Mth.DEG_TO_RAD, head.xRot() * 2 / 3 + Mth.HALF_PI, false));
            arm.setRotationY(MathHelper.lerpRad(t, 0, mult * 15 * Mth.DEG_TO_RAD, false));
            forearm.setRotationX(MathHelper.lerpRad(t * t, 130 * Mth.DEG_TO_RAD, 0, false));
            if (both) {
                if (followUp) {
                    opArm.setRotationX(MathHelper.lerpRad(t, head.xRot() * 2 / 3 + Mth.HALF_PI, -20 * Mth.DEG_TO_RAD, false));
                    opArm.setRotationY(MathHelper.lerpRad(t, mult * 15 * Mth.DEG_TO_RAD, 0, false));
                    opForearm.setRotationX(MathHelper.lerpRad(t, 0, 130 * Mth.DEG_TO_RAD, false));
                }
                else {
                    opArm.setRotationX(-20 * Mth.DEG_TO_RAD);
                    opArm.setRotationY(-mult * 15 * Mth.DEG_TO_RAD);
                    opForearm.setRotationX(130 * Mth.DEG_TO_RAD);
                }
            }
        }
        else {
            float r = mult * -30 * Mth.DEG_TO_RAD;
            body.setRotationY(r);
            legR.setRotationY(-r);
            legL.setRotationY(-r);
            arm.setRotationX(head.xRot() * 2 / 3 + Mth.HALF_PI);
            arm.setRotationY(mult * 15 * Mth.DEG_TO_RAD);
            if (both) {
                opArm.setRotationX(-20 * Mth.DEG_TO_RAD);
                opArm.setRotationY(-mult * 15 * Mth.DEG_TO_RAD);
                opForearm.setRotationX(130 * Mth.DEG_TO_RAD);
            }
        }
    }

    public static void animateZombieArms(HM leftArm, HM rightArm, boolean isAggresive, float attackTime, float ageInTicks) {
        float f = Mth.sin(attackTime * Mth.PI);
        float f1 = Mth.sin((1.0F - (1.0F - attackTime) * (1.0F - attackTime)) * Mth.PI);
        rightArm.setRotationZ(0.0F);
        leftArm.setRotationZ(0.0F);
        rightArm.setRotationY(-(0.1F - f * 0.6F));
        leftArm.setRotationY(-(0.1F - f * 0.6F));
        float f2 = Mth.PI / (isAggresive ? 1.5F : 2.25F);
        rightArm.setRotationX(f2);
        leftArm.setRotationX(f2);
        rightArm.addRotationX(-(f * 1.2F - f1 * 0.4F));
        leftArm.addRotationX(-(f * 1.2F - f1 * 0.4F));
        bobArms(rightArm, leftArm, ageInTicks);
    }

    public static void bobArms(HM rightArm, HM leftArm, float ageInTicks) {
        bobModelPart(rightArm, ageInTicks, 1.0F);
        bobModelPart(leftArm, ageInTicks, -1.0F);
    }

    public static void bobModelPart(HM part, float ageInTicks, float mult) {
        part.addRotationZ(mult * (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F));
        part.addRotationX(-mult * Mth.sin(ageInTicks * 0.067F) * 0.05F);
    }

    public static void setupItemPosition(HM holder, HumanoidArm arm, ItemStack stack) {
        if (stack.getItem() instanceof IEvolutionItem item && item.usesModularRendering()) {
            holder.translateX(arm == HumanoidArm.RIGHT ? item.getRenderOffsetX() : -item.getRenderOffsetX());
            holder.translateY(item.getRenderOffsetY());
            holder.translateZ(item.getRenderOffsetZ());
        }
        if (stack.getItem() instanceof ItemModularTool && IModularTool.get(stack).isShovel()) {
            holder.setRotationZ(-90 * Mth.DEG_TO_RAD);
        }
    }

    public static void strikeDown(float progress, int mult, HM body, HM legR, HM legL, HM arm, HM forearm) {
        strikeDown(progress, mult, body, legR, legL, arm, forearm, 18 * Mth.DEG_TO_RAD, 0, 0);
    }

    public static void strikeDown(float progress, int mult, HM body, HM legR, HM legL, HM arm, HM forearm, float xR, float yR, float zR) {
        if (progress < 0.5f) {
            float t = MathHelper.animInterval(progress, 0, 0.5f);
            body.setRotationY(mult * MathHelper.lerpRad(t, 0, -30 * Mth.DEG_TO_RAD, false));
            float leg = mult * MathHelper.lerpRad(t, 0, 15 * Mth.DEG_TO_RAD, false);
            legR.setRotationY(leg);
            legL.setRotationY(leg);
            arm.setRotationX(MathHelper.lerpRad(t, xR, 139.106_6f * Mth.DEG_TO_RAD, false));
            arm.setRotationY(MathHelper.lerpRad(t, yR, mult * -20.704_8f * Mth.DEG_TO_RAD, false));
            arm.setRotationZ(MathHelper.lerpRad(t, zR, mult * -22.207_7f * Mth.DEG_TO_RAD, false));
            forearm.setRotationX(MathHelper.lerpRad(t, 0, 45.409_8f * Mth.DEG_TO_RAD, false));
            forearm.setRotationY(MathHelper.lerpRad(t, 0, mult * 10.288_6f * Mth.DEG_TO_RAD, false));
            forearm.setRotationZ(MathHelper.lerpRad(t, 0, mult * -22.909_8f * Mth.DEG_TO_RAD, false));
        }
        else if (progress < 0.75f) {
            float t = MathHelper.animInterval(progress, 0.5f, 0.75f);
            body.setRotationY(mult * MathHelper.lerpRad(t, -30 * Mth.DEG_TO_RAD, 60 * Mth.DEG_TO_RAD, false));
            float leg = mult * MathHelper.lerpRad(t, 15 * Mth.DEG_TO_RAD, -30 * Mth.DEG_TO_RAD, false);
            legR.setRotationY(leg);
            legL.setRotationY(leg);
            arm.setRotationX(MathHelper.lerpRad(t, 139.106_6f * Mth.DEG_TO_RAD, 24.679_1f * Mth.DEG_TO_RAD, false));
            arm.setRotationY(mult * MathHelper.lerpRad(t, -20.704_8f * Mth.DEG_TO_RAD, -42.260_2f * Mth.DEG_TO_RAD, false));
            arm.setRotationZ(mult * MathHelper.lerpRad(t, -22.207_7f * Mth.DEG_TO_RAD, -17.172f * Mth.DEG_TO_RAD, false));
            forearm.setRotationX(MathHelper.lerpRad(t, 45.409_8f * Mth.DEG_TO_RAD, 0, false));
            forearm.setRotationY(MathHelper.lerpRad(t, mult * 10.288_6f * Mth.DEG_TO_RAD, 0, false));
            forearm.setRotationZ(MathHelper.lerpRad(t, mult * -22.909_8f * Mth.DEG_TO_RAD, 0, false));
        }
        else {
            body.setRotationY(60 * Mth.DEG_TO_RAD * mult);
            float leg = mult * -30 * Mth.DEG_TO_RAD;
            legR.setRotationY(leg);
            legL.setRotationY(leg);
            arm.setRotationX(24.679_1f * Mth.DEG_TO_RAD);
            arm.setRotationY(mult * -42.260_2f * Mth.DEG_TO_RAD);
            arm.setRotationZ(mult * -17.172f * Mth.DEG_TO_RAD);
            forearm.setRotationX(0);
            forearm.setRotationY(0);
            forearm.setRotationZ(0);
        }
    }

    public static void strikeFromFarSide(float progress, int mult, HM body, HM legR, HM legL, HM arm, HM forearm) {
        if (progress < 0.5f) {
            float t = MathHelper.animInterval(progress, 0, 0.5f);
            body.setRotationY(mult * 60 * Mth.DEG_TO_RAD);
            float leg = mult * -30 * Mth.DEG_TO_RAD;
            legR.setRotationY(leg);
            legL.setRotationY(leg);
            arm.setRotationX(MathHelper.lerpRad(t, 24.679_1f * Mth.DEG_TO_RAD, 37.538_7f * Mth.DEG_TO_RAD, false));
            arm.setRotationY(mult * MathHelper.lerpRad(t, -42.260_2f * Mth.DEG_TO_RAD, -15.221f * Mth.DEG_TO_RAD, false));
            arm.setRotationZ(mult * MathHelper.lerpRad(t, -17.172f * Mth.DEG_TO_RAD, 64.054f * Mth.DEG_TO_RAD, false));
            forearm.setRotationX(MathHelper.lerpRad(t, 0, 45 * Mth.DEG_TO_RAD, false));
        }
        else if (progress < 0.75f) {
            float t = MathHelper.animInterval(progress, 0.5f, 0.75f);
            body.setRotationY(mult * MathHelper.lerpRad(t, 60 * Mth.DEG_TO_RAD, -30 * Mth.DEG_TO_RAD, false));
            float leg = mult * MathHelper.lerpRad(t, -30 * Mth.DEG_TO_RAD, 15 * Mth.DEG_TO_RAD, false);
            legR.setRotationY(leg);
            legL.setRotationY(leg);
            arm.setRotationX(MathHelper.lerpRad(t, 37.538_7f * Mth.DEG_TO_RAD, 40.804_9f * Mth.DEG_TO_RAD, false));
            arm.setRotationY(mult * MathHelper.lerpRad(t, -15.221f * Mth.DEG_TO_RAD, 14.666_2f * Mth.DEG_TO_RAD, false));
            arm.setRotationZ(mult * MathHelper.lerpRad(t, 64.054f * Mth.DEG_TO_RAD, 43.341_7f * Mth.DEG_TO_RAD, false));
            forearm.setRotationX(MathHelper.lerpRad(t, 45 * Mth.DEG_TO_RAD, 0, false));
        }
        else {
            body.setRotationY(mult * -30 * Mth.DEG_TO_RAD);
            float leg = mult * 15 * Mth.DEG_TO_RAD;
            legR.setRotationY(leg);
            legL.setRotationY(leg);
            arm.setRotationX(40.804_9f * Mth.DEG_TO_RAD);
            arm.setRotationY(mult * 14.666_2f * Mth.DEG_TO_RAD);
            arm.setRotationZ(mult * 43.341_7f * Mth.DEG_TO_RAD);
            forearm.setRotationX(0);
        }
    }

    public static void thrust(float progress,
                              int mult,
                              HM body,
                              HM legR,
                              HM legL,
                              HM arm,
                              HM forearm,
                              HM holder,
                              float headPitch) {
        if (progress < 0.5f) {
            float t = MathHelper.animInterval(progress, 0, 0.5f);
            body.setRotationY(MathHelper.lerpRad(t, 0, -10 * Mth.DEG_TO_RAD * mult, false));
            float leg = MathHelper.lerpRad(t, 0, 5 * Mth.DEG_TO_RAD * mult, false);
            legR.setRotationY(leg);
            legL.setRotationY(leg);
            arm.setRotationX(MathHelper.lerpRad(t, 18 * Mth.DEG_TO_RAD, -50 * Mth.DEG_TO_RAD, false));
            arm.setRotationZ(MathHelper.lerpRad(t, 0, 80 * Mth.DEG_TO_RAD * mult, false));
            forearm.setRotationX(MathHelper.lerpRad(t, 0, 52.5f * Mth.DEG_TO_RAD, false));
        }
        else if (progress < 0.75f) {
            float t = MathHelper.animInterval(progress, 0.5f, 0.75f);
            body.setRotationY(mult * MathHelper.lerpRad(t, -10 * Mth.DEG_TO_RAD, 30 * Mth.DEG_TO_RAD, false));
            float leg = mult * MathHelper.lerpRad(t, 5 * Mth.DEG_TO_RAD, -15 * Mth.DEG_TO_RAD, false);
            legR.setRotationY(leg);
            legL.setRotationY(leg);
            arm.setRotationX(MathHelper.lerpRad(t, -50 * Mth.DEG_TO_RAD, headPitch * 2 / 3 + Mth.HALF_PI, false));
            arm.setRotationY(MathHelper.lerpRad(t, 0, -20 * Mth.DEG_TO_RAD * mult, false));
            arm.setRotationZ(MathHelper.lerpRad(t, 80 * Mth.DEG_TO_RAD * mult, 0, false));
            forearm.setRotationX(MathHelper.lerpRad(t, 52.5f * Mth.DEG_TO_RAD, 0, false));
            holder.setRotationX(MathHelper.lerpRad(t, 0, -90 * Mth.DEG_TO_RAD, false));
        }
        else {
            body.setRotationY(30 * Mth.DEG_TO_RAD * mult);
            float leg = -15 * Mth.DEG_TO_RAD * mult;
            legR.setRotationY(leg);
            legL.setRotationY(leg);
            arm.setRotationX(headPitch * 2 / 3 + Mth.HALF_PI);
            arm.setRotationY(-20 * Mth.DEG_TO_RAD * mult);
            arm.setRotationZ(0);
            forearm.setRotationX(0);
            holder.setRotationX(-90 * Mth.DEG_TO_RAD);
        }
    }
}
