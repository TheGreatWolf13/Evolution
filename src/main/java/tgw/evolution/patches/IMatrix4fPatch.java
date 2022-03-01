package tgw.evolution.patches;

import com.mojang.math.Quaternion;

public interface IMatrix4fPatch {

    void rotate(Quaternion quaternion);

    float transformVecX(float x, float y, float z);

    float transformVecY(float x, float y, float z);

    float transformVecZ(float x, float y, float z);

    void translate(float x, float y, float z);
}
