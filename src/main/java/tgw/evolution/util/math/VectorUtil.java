package tgw.evolution.util.math;

import com.mojang.math.Vector4f;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public final class VectorUtil {

    private VectorUtil() {
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

    public static double length(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static double normalizeComponent(double x, double length) {
        return length < 1.0E-4 ? 0 : x / length;
    }

    public static double subtractLengthSqr(Vec3 vec, double x, double y, double z) {
        double newX = vec.x - x;
        double newY = vec.y - y;
        double newZ = vec.z - z;
        return newX * newX + newY * newY + newZ * newZ;
    }
}
