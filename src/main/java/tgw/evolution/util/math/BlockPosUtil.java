package tgw.evolution.util.math;

import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;

public final class BlockPosUtil {

    private BlockPosUtil() {}

    public static int compare(int x, int y, int z, Vec3i pos) {
        if (y == pos.getY()) {
            return z == pos.getZ() ? x - pos.getX() : z - pos.getZ();
        }
        return y - pos.getY();
    }

    public static int getChessBoardDistance(ChunkPos pos, int chunkX, int chunkZ) {
        return Math.max(Math.abs(pos.x - chunkX), Math.abs(pos.z - chunkZ));
    }

    public static int getX(int secX, short relative) {
        return SectionPos.sectionToBlockCoord(secX) + SectionPos.sectionRelativeX(relative);
    }

    public static int getY(int secY, short relative) {
        return SectionPos.sectionToBlockCoord(secY) + SectionPos.sectionRelativeY(relative);
    }

    public static int getZ(int secZ, short relative) {
        return SectionPos.sectionToBlockCoord(secZ) + SectionPos.sectionRelativeZ(relative);
    }

    public static short sectionRelativePos(int x, int y, int z) {
        int i = SectionPos.sectionRelative(x);
        int j = SectionPos.sectionRelative(y);
        int k = SectionPos.sectionRelative(z);
        return (short) (i << 8 | k << 4 | j);
    }
}
