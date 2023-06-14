package tgw.evolution.patches;

import net.minecraft.util.Mth;

public interface IPoseStackPatch {

    default void mulPoseX(float degree) {
        this.mulPoseXRad(Mth.DEG_TO_RAD * degree);
    }

    void mulPoseXRad(float radian);

    default void mulPoseY(float degree) {
        this.mulPoseYRad(Mth.DEG_TO_RAD * degree);
    }

    void mulPoseYRad(float radian);

    default void mulPoseZ(float degree) {
        this.mulPoseZRad(Mth.DEG_TO_RAD * degree);
    }

    void mulPoseZRad(float radian);

    void reset();
}
