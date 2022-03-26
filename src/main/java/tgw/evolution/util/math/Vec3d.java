package tgw.evolution.util.math;

import com.mojang.math.Vector3f;

public class Vec3d {

    public static final Vec3d ZERO = new Vec3d();
    private double x;
    private double y;
    private double z;

    public Vec3d() {
        this(0, 0, 0);
    }

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vec3d fromRGB24(int packed, Vec3d vec) {
        double dx = (packed >> 16 & 255) / 255.0;
        double dy = (packed >> 8 & 255) / 255.0;
        double dz = (packed & 255) / 255.0;
        return vec.set(dx, dy, dz);
    }

    public static Vec3d fromRGB24(int packed) {
        return fromRGB24(packed, new Vec3d());
    }

    public Vec3d add(Vec3d vec) {
        return this.add(vec.x, vec.y, vec.z);
    }

    public Vec3d add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public Vec3d scale(double mult) {
        this.x *= mult;
        this.y *= mult;
        this.z *= mult;
        return this;
    }

    public Vec3d set(Vector3f vec) {
        return this.set(vec.x(), vec.y(), vec.z());
    }

    public Vec3d set(Vec3d vec) {
        return this.set(vec.x, vec.y, vec.z);
    }

    public Vec3d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3d sub(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3d sub(Vec3d vec) {
        return this.sub(vec.x, vec.y, vec.z);
    }
}
