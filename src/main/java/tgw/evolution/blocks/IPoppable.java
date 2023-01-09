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

    default void popDrops(BlockState state, Level level, BlockPos pos) {
        Block.dropResources(state, level, pos);
    }

    @Override
    default boolean popLogic(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.canSurvive(level, pos)) {
            this.popDrops(state, level, pos);
            level.removeBlock(pos, false);
            return true;
        }
        return false;
    }

    @Override
    default boolean pops() {
        return true;
    }
}
