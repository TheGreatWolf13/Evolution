package tgw.evolution.patches;

import net.minecraft.util.Mth;

public interface PatchPoseStack {

    default void mulPoseX(float degree) {
        this.mulPoseXRad(Mth.DEG_TO_RAD * degree);
    }

    default void mulPoseXRad(float radian) {
        throw new AbstractMethodError();
    }

    default void mulPoseY(float degree) {
        this.mulPoseYRad(Mth.DEG_TO_RAD * degree);
    }

    default void mulPoseYRad(float radian) {
        throw new AbstractMethodError();
    }

    default void mulPoseZ(float degree) {
        this.mulPoseZRad(Mth.DEG_TO_RAD * degree);
    }

    default void mulPoseZRad(float radian) {
        throw new AbstractMethodError();
    }

    default void reset() {
        throw new AbstractMethodError();
    }

    default void rotate(float i, float j, float k, float r) {
        throw new AbstractMethodError();
    }

    default void rotateXYZ(float x, float y, float z, boolean degrees) {
        if (degrees) {
            x *= Mth.DEG_TO_RAD;
            y *= Mth.DEG_TO_RAD;
            z *= Mth.DEG_TO_RAD;
        }
        x *= 0.5f;
        y *= 0.5f;
        z *= 0.5f;
        float i = (float) Math.sin(x);
        float j = (float) Math.cos(x);
        float k = (float) Math.sin(y);
        float l = (float) Math.cos(y);
        float m = (float) Math.sin(z);
        float n = (float) Math.cos(z);
        this.rotate(i * l * n + j * k * m, j * k * n - i * l * m, i * k * n + j * l * m, j * l * n - i * k * m);
    }
}
