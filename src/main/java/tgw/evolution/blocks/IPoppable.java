package tgw.evolution.blocks;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.blocks.util.BlockUtils;

/**
 * Represents a block that must be supported, or else it pops as an item.
 */
public interface IPoppable extends IPhysics {

    default void popDrops(BlockState state, Level level, int x, int y, int z) {
        BlockUtils.dropResources(state, level, x, y, z);
    }

    default boolean popLogic(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState_(x, y, z);
        if (!state.canSurvive_(level, x, y, z)) {
            this.popDrops(state, level, x, y, z);
            level.removeBlock_(x, y, z, false);
            return true;
        }
        return false;
    }
}
