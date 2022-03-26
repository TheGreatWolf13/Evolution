package tgw.evolution.util.math;

import net.minecraft.util.Mth;

public class Vec3f {
    public float x;
    public float y;
    public float z;

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float dotProduct(Vec3f vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public float inverseLength() {
        return Mth.fastInvSqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }
}
