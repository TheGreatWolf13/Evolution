package tgw.evolution.util.math;

import net.minecraft.world.phys.AABB;
import tgw.evolution.patches.IAABBPatch;

@SuppressWarnings("EqualsAndHashcode")
public class AABBMutable extends AABB {

    public AABBMutable() {
        this(0, 0, 0, 0, 0, 0);
    }

    public AABBMutable(double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
    }

    public static AABBMutable block(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AABBMutable(x1 / 16, y1 / 16, z1 / 16, x2 / 16, y2 / 16, z2 / 16);
    }

    public AABBMutable deflateMutable(double value) {
        return this.inflateMutable(-value);
    }

    @Override
    public int hashCode() {
        throw new IllegalStateException("Cannot hash mutable object");
    }

    public AABBMutable inflateMutable(double value) {
        return this.inflateMutable(value, value, value);
    }

    public AABBMutable inflateMutable(double x, double y, double z) {
        return this.set(this.minX - x, this.minY - y, this.minZ - z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public AABBMutable set(double x1, double y1, double z1, double x2, double y2, double z2) {
        ((IAABBPatch) this).setMinX(Math.min(x1, x2));
        ((IAABBPatch) this).setMinY(Math.min(y1, y2));
        ((IAABBPatch) this).setMinZ(Math.min(z1, z2));
        ((IAABBPatch) this).setMaxX(Math.max(x1, x2));
        ((IAABBPatch) this).setMaxY(Math.max(y1, y2));
        ((IAABBPatch) this).setMaxZ(Math.max(z1, z2));
        return this;
    }
}
