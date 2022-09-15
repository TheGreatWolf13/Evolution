package tgw.evolution.util.hitbox.hms;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;

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
}
