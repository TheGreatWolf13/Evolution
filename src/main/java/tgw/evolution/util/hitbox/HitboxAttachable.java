package tgw.evolution.util.hitbox;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.util.math.Vec3d;

public class HitboxAttachable extends Hitbox {

    //Mutable internally, immutable externally
    private final Vec3d localOrigin = new Vec3d();

    public HitboxAttachable(HitboxType part,
                            AABB aabb,
                            HitboxEntity<?> parent,
                            double localOriginX, double localOriginY, double localOriginZ) {
        super(part, aabb, parent);
        this.localOrigin.set(localOriginX, localOriginY, localOriginZ);
    }

    public Vec3 getLocalOrigin() {
        return this.localOrigin;
    }
}
