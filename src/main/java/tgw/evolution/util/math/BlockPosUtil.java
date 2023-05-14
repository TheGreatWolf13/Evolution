package tgw.evolution.util.math;

import net.minecraft.core.Vec3i;

public final class BlockPosUtil {

    private BlockPosUtil() {}

    public static int compare(int x, int y, int z, Vec3i pos) {
        if (y == pos.getY()) {
            return z == pos.getZ() ? x - pos.getX() : z - pos.getZ();
        }
        return y - pos.getY();
    }
}
