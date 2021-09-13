package tgw.evolution.entities;

import net.minecraft.util.math.vector.Vector3d;

public interface INeckPosition {

    float getCameraYOffset();

    float getCameraZOffset();

    Vector3d getNeckPoint();
}
