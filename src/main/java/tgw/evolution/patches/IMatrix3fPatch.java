package tgw.evolution.patches;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

public interface IMatrix3fPatch {

    float getM00();

    float getM01();

    float getM02();

    float getM10();

    float getM11();

    float getM12();

    float getM20();

    float getM21();

    float getM22();

    void rotate(Quaternion quaternion);

    void rotateX(float i, float r);

    void rotateY(float j, float r);

    void rotateZ(float k, float r);

    void scale(float x, float y, float z);

    float transformVecX(float x, float y, float z);

    default float transformVecX(Vector3f dir) {
        return this.transformVecX(dir.x(), dir.y(), dir.z());
    }

    float transformVecY(float x, float y, float z);

    default float transformVecY(Vector3f dir) {
        return this.transformVecY(dir.x(), dir.y(), dir.z());
    }

    float transformVecZ(float x, float y, float z);

    default float transformVecZ(Vector3f dir) {
        return this.transformVecZ(dir.x(), dir.y(), dir.z());
    }

}
