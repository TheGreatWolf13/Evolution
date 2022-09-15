package tgw.evolution.patches;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

public interface IQuaternionPatch {

    Quaternion set(Vector3f axis, float angle, boolean degrees);
}
