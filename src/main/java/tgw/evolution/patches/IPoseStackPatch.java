package tgw.evolution.patches;

import tgw.evolution.util.math.MathHelper;

public interface IPoseStackPatch {

    default void mulPoseX(float degree) {
        this.mulPoseXRad(MathHelper.degToRad(degree));
    }

    void mulPoseXRad(float radian);

    default void mulPoseY(float degree) {
        this.mulPoseYRad(MathHelper.degToRad(degree));
    }

    void mulPoseYRad(float radian);

    default void mulPoseZ(float degree) {
        this.mulPoseZRad(MathHelper.degToRad(degree));
    }

    void mulPoseZRad(float radian);
}
