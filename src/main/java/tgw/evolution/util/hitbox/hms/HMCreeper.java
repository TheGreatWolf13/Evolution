package tgw.evolution.util.hitbox.hms;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public interface HMCreeper<T extends Entity> extends HMHierarchical<T> {

    HM head();

    HM leftFrontLeg();

    HM leftHindLeg();

    HM rightFrontLeg();

    HM rightHindLeg();

    @Override
    default void setup(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head().setRotationY(netHeadYaw * Mth.DEG_TO_RAD);
        this.head().setRotationX(headPitch * Mth.DEG_TO_RAD);
        this.rightHindLeg().setRotationX(Mth.cos(limbSwing * 0.666_2F) * 1.4F * limbSwingAmount);
        this.leftHindLeg().setRotationX(Mth.cos(limbSwing * 0.666_2F + Mth.PI) * 1.4F * limbSwingAmount);
        this.rightFrontLeg().setRotationX(Mth.cos(limbSwing * 0.666_2F + Mth.PI) * 1.4F * limbSwingAmount);
        this.leftFrontLeg().setRotationX(Mth.cos(limbSwing * 0.666_2F) * 1.4F * limbSwingAmount);
    }
}
