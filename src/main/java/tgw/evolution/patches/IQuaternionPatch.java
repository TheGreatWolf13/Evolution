package tgw.evolution.patches;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

public interface IQuaternionPatch {

    void mul(Vector3f vec, float angleRad);

    Quaternion set(Vector3f axis, float angle, boolean degrees);
}
