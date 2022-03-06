package tgw.evolution.patches;

import com.mojang.math.Vector3f;

public interface IQuaternionPatch {

    void set(Vector3f axis, float angle, boolean degrees);
}
