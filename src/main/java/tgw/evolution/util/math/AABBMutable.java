package tgw.evolution.util.math;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.mixin.AccessorAABB;

@SuppressWarnings("EqualsAndHashcode")
public class AABBMutable extends AABB {

    public AABBMutable() {
        this(0, 0, 0, 0, 0, 0);
    }

    public AABBMutable(AABB bb) {
        this(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    public AABBMutable(Vec3 start, Vec3 end) {
        this(start.x, start.y, start.z, end.x, end.y, end.z);
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
        return this.setUnchecked(this.minX - x, this.minY - y, this.minZ - z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    @Override
    public AABBMutable minmax(AABB other) {
        double x0 = Math.min(this.minX, other.minX);
        double y0 = Math.min(this.minY, other.minY);
        double z0 = Math.min(this.minZ, other.minZ);
        double x1 = Math.max(this.maxX, other.maxX);
        double y1 = Math.max(this.maxY, other.maxY);
        double z1 = Math.max(this.maxZ, other.maxZ);
        return this.setUnchecked(x0, y0, z0, x1, y1, z1);
    }

    public AABBMutable moveMutable(double dx, double dy, double dz) {
        ((AccessorAABB) this).setMinX(this.minX + dx);
        ((AccessorAABB) this).setMaxX(this.maxX + dx);
        ((AccessorAABB) this).setMinY(this.minY + dy);
        ((AccessorAABB) this).setMaxY(this.maxY + dy);
        ((AccessorAABB) this).setMinZ(this.minZ + dz);
        ((AccessorAABB) this).setMaxZ(this.maxZ + dz);
        return this;
    }

    @CanIgnoreReturnValue
    public AABBMutable moveY(double dy) {
        ((AccessorAABB) this).setMinY(this.minY + dy);
        ((AccessorAABB) this).setMaxY(this.maxY + dy);
        return this;
    }

    public AABBMutable set(AABB aabb) {
        return this.setUnchecked(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    public AABBMutable set(double x1, double y1, double z1, double x2, double y2, double z2) {
        ((AccessorAABB) this).setMinX(Math.min(x1, x2));
        ((AccessorAABB) this).setMinY(Math.min(y1, y2));
        ((AccessorAABB) this).setMinZ(Math.min(z1, z2));
        ((AccessorAABB) this).setMaxX(Math.max(x1, x2));
        ((AccessorAABB) this).setMaxY(Math.max(y1, y2));
        ((AccessorAABB) this).setMaxZ(Math.max(z1, z2));
        return this;
    }

    /**
     * Same as {@link AABBMutable#set(double, double, double, double, double, double)}, but doesn't check for min max.
     * Only use if you're sure the parameters are in the right order.
     */
    public AABBMutable setUnchecked(double x1, double y1, double z1, double x2, double y2, double z2) {
        ((AccessorAABB) this).setMinX(x1);
        ((AccessorAABB) this).setMinY(y1);
        ((AccessorAABB) this).setMinZ(z1);
        ((AccessorAABB) this).setMaxX(x2);
        ((AccessorAABB) this).setMaxY(y2);
        ((AccessorAABB) this).setMaxZ(z2);
        return this;
    }

    public AABBMutable setX(double x1, double x2) {
        ((AccessorAABB) this).setMinX(Math.min(x1, x2));
        ((AccessorAABB) this).setMaxX(Math.max(x1, x2));
        return this;
    }
}
