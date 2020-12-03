package tgw.evolution.util;

public class Vec3f {
    public float x;
    public float y;
    public float z;

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3f crossProduct(Vec3f vec1, Vec3f vec2) {
        this.x = vec1.y * vec2.z - vec1.z * vec2.y;
        this.y = vec1.z * vec2.x - vec1.x * vec2.z;
        this.z = vec1.x * vec2.y - vec1.y * vec2.x;
        return this;
    }

    public float dotProduct(Vec3f vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public float inverseLength() {
        return (float) net.minecraft.util.math.MathHelper.fastInvSqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public float length() {
        return MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }
}
