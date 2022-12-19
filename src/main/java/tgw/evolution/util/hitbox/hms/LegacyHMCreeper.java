package tgw.evolution.util.hitbox.hms;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public interface LegacyHMCreeper<T extends Entity> extends HMHierarchical<T> {

    HM head();

    HM legFL();

    HM legFR();

    HM legHL();

    HM legHR();

    @Override
    default void setup(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        HM head = this.head();
        head.setRotationY(netHeadYaw * Mth.DEG_TO_RAD);
        head.setRotationX(headPitch * Mth.DEG_TO_RAD);
        float legRot = 1.4F * limbSwingAmount * Mth.cos(limbSwing * 0.666_2F);
        this.legHR().setRotationX(legRot);
        this.legHL().setRotationX(-legRot);
        this.legFR().setRotationX(-legRot);
        this.legFL().setRotationX(legRot);
    }
}
