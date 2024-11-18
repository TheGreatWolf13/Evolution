package tgw.evolution.util.math;

import com.mojang.math.Vector4f;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class VectorUtil {

    private VectorUtil() {
    }

    public static double dist(Vec3 vec, double x, double y, double z) {
        double dx = vec.x - x;
        double dy = vec.y - y;
        double dz = vec.z - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static double distSqr(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return dx * dx + dy * dy + dz * dz;
    }

    public static double distSqr(Vec3i vec, int x, int y, int z) {
        double dx = vec.getX() + 0.5 - x;
        double dy = vec.getY() + 0.5 - y;
        double dz = vec.getZ() + 0.5 - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public static float dot(Vector4f vec, float x, float y, float z) {
        return vec.x() * x + vec.y() * y + vec.z() * z + vec.w();
    }

    public static double horizontalLength(double x, double z) {
        return Math.sqrt(x * x + z * z);
    }

    public static double length(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static double lengthSqr(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    public static float norm(float x, float y, float z) {
        float norm = Mth.fastInvSqrt(x * x + y * y + z * z);
        return norm > 1e5 ? 0 : norm;
    }

    public static double norm(double x, double y, double z) {
        double norm = Mth.fastInvSqrt(x * x + y * y + z * z);
        return norm > 1e4 ? 0 : norm;
    }

    public static double subtractLengthSqr(Vec3 vec, double x, double y, double z) {
        double newX = vec.x - x;
        double newY = vec.y - y;
        double newZ = vec.z - z;
        return newX * newX + newY * newY + newZ * newZ;
    }
}
