package tgw.evolution.patches;

import com.mojang.math.Quaternion;

public interface IMatrix4fPatch {

    void multiplyWithPerspective(double fov, float aspectRatio, float nearPlane, float farPlane);

    void rotate(Quaternion quaternion);

    void rotateX(float i, float r);

    void rotateY(float j, float r);

    void rotateZ(float k, float r);

    void scale(float x, float y, float z);

    float transformVecX(float x, float y, float z);

    float transformVecY(float x, float y, float z);

    float transformVecZ(float x, float y, float z);
}
