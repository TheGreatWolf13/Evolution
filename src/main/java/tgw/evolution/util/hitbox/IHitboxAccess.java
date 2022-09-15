package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.Entity;
import tgw.evolution.util.math.Vec3d;

public interface IHitboxAccess<T extends Entity> {

    Vec3d helperOffset();

    void init(T entity, float partialTicks);

    double transformX(double x, double y, double z);

    double transformY(double x, double y, double z);

    double transformZ(double x, double y, double z);
}
