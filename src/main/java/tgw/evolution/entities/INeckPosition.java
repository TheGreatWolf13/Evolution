package tgw.evolution.entities;

import net.minecraft.world.phys.Vec3;

public interface INeckPosition {

    float getCameraYOffset();

    float getCameraZOffset();

    Vec3 getNeckPoint();
}
