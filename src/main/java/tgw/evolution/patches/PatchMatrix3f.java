package tgw.evolution.patches;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

public interface PatchMatrix3f {

    default void rotate(Quaternion quaternion) {
        this.rotate(quaternion.i(), quaternion.j(), quaternion.k(), quaternion.r());
    }

    default void rotate(float i, float j, float k, float r) {
        throw new AbstractMethodError();
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

    default float transformVecX(Vector3f dir) {
        return this.transformVecX(dir.x(), dir.y(), dir.z());
    }

    default float transformVecY(float x, float y, float z) {
        throw new AbstractMethodError();
    }

    default float transformVecY(Vector3f dir) {
        return this.transformVecY(dir.x(), dir.y(), dir.z());
    }

    default float transformVecZ(float x, float y, float z) {
        throw new AbstractMethodError();
    }

    default float transformVecZ(Vector3f dir) {
        return this.transformVecZ(dir.x(), dir.y(), dir.z());
    }
}
