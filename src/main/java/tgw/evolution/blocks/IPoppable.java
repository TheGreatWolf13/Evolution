package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a block that must be supported, or else it pops as an item.
 */
public interface IPoppable extends IPhysics {

    @Override
    default @Nullable SoundEvent fallingSound() {
        return null;
    }

    default void popDrops(BlockState state, Level level, int x, int y, int z) {
        //todo
        Block.dropResources(state, level, new BlockPos(x, y, z));
    }

    @Override
    default boolean popLogic(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState_(x, y, z);
        if (!state.canSurvive_(level, x, y, z)) {
            this.popDrops(state, level, x, y, z);
            level.removeBlock(new BlockPos(x, y, z), false);
            return true;
        }
        return false;
    }

    @Override
    default boolean pops() {
        return true;
    }
}
