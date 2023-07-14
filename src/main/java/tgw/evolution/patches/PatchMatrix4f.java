package tgw.evolution.patches;

import com.mojang.math.Quaternion;

public interface PatchMatrix4f {

    default void multiplyWithPerspective(double fov, float aspectRatio, float nearPlane, float farPlane) {
        throw new AbstractMethodError();
    }

    default void rotate(float i, float j, float k, float r) {
        throw new AbstractMethodError();
    }

    default void rotate(Quaternion quaternion) {
        this.rotate(quaternion.i(), quaternion.j(), quaternion.k(), quaternion.r());
    }

    default void rotateX(float i, float r) {
        throw new AbstractMethodError();
    }

    default void rotateY(float j, float r) {
        throw new AbstractMethodError();
    }

    default void rotateZ(float k, float r) {
        throw new AbstractMethodError();
    }

    default void scale(float x, float y, float z) {
        throw new AbstractMethodError();
    }

    default float transformVecX(float x, float y, float z) {
        throw new AbstractMethodError();
    }

    default float transformVecY(float x, float y, float z) {
        throw new AbstractMethodError();
    }

    default float transformVecZ(float x, float y, float z) {
        throw new AbstractMethodError();
    }
}
