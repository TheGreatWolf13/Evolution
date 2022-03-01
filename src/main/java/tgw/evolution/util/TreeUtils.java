package tgw.evolution.util;

import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public final class TreeUtils {

    private TreeUtils() {
    }

    public static void iterateBlocks(int range, BlockPos center, Consumer<BlockPos.MutableBlockPos> action) {
        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
        int y = -range;
        while (y <= range) {
            for (int x = -range; x <= range; ++x) {
                for (int z = -range; z <= range; ++z) {
                    targetPos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    action.accept(targetPos);
                }
            }
            y++;
        }
    }
}
