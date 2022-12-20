package tgw.evolution.util.math;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.math.Vector3f;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.patches.IVec3Patch;

@SuppressWarnings("EqualsAndHashcode")
public class Vec3d extends Vec3 {

    public static final Vec3 NULL = new Vec3(Double.NaN, Double.NaN, Double.NaN);

    public Vec3d() {
        this(0, 0, 0);
    }

    public Vec3d(Vec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vec3d(double x, double y, double z) {
        super(x, y, z);
    }

    public static Vec3d fromRGB24(int packed, Vec3d vec) {
        double dx = (packed >> 16 & 255) / 255.0;
        double dy = (packed >> 8 & 255) / 255.0;
        double dz = (packed & 255) / 255.0;
        return vec.set(dx, dy, dz);
    }

    public Vec3d addMutable(Vec3 vec) {
        return this.addMutable(vec.x, vec.y, vec.z);
    }

    public Vec3d addMutable(double x, double y, double z) {
        return this.set(this.x + x, this.y + y, this.z + z);
    }

    public Vec3 asImmutable() {
        return new Vec3(this.x, this.y, this.z);
    }

    public Vec3d crossMutable(Vec3 vec) {
        return this.set(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
    }

    public Vec3d divideMutable(double x, double y, double z) {
        return this.set(this.x / x, this.y / y, this.z / z);
    }

    @Override
    public int hashCode() {
        throw new IllegalStateException("Cannot hash mutable object");
    }

    public boolean isNull() {
        return Double.isNaN(this.x);
    }

    public Vec3d multiplyMutable(double x, double y, double z) {
        return this.set(this.x * x, this.y * y, this.z * z);
    }

    public Vec3d multiplyMutable(Vec3 vec) {
        return this.multiplyMutable(vec.x, vec.y, vec.z);
    }

    public Vec3d normalizeMutable() {
        double norm = Mth.fastInvSqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        return norm > 1.0E4 ? this.set(0, 0, 0) : this.set(this.x * norm, this.y * norm, this.z * norm);
    }

    @CanIgnoreReturnValue
    public Vec3d scaleMutable(double scale) {
        return this.multiplyMutable(scale, scale, scale);
    }

    public Vec3d set(Vec3 vec) {
        return this.set(vec.x, vec.y, vec.z);
    }

    public Vec3d set(Vector3f vec) {
        return this.set(vec.x(), vec.y(), vec.z());
    }

    public Vec3d set(double x, double y, double z) {
        ((IVec3Patch) this).setPosX(x);
        ((IVec3Patch) this).setPosY(y);
        ((IVec3Patch) this).setPosZ(z);
        return this;
    }

    public Vec3d subMutable(Vec3 vec) {
        return this.subMutable(vec.x, vec.y, vec.z);
    }

    public Vec3d subMutable(double x, double y, double z) {
        return this.addMutable(-x, -y, -z);
    }
}
