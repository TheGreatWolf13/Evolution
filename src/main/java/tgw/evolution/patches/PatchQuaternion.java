package tgw.evolution.patches;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

public interface PatchQuaternion {

    default void mul(Vector3f vec, float angleRad) {
        throw new AbstractMethodError();
    }

    default Quaternion set(Vector3f axis, float angle, boolean degrees) {
        throw new AbstractMethodError();
    }
}
