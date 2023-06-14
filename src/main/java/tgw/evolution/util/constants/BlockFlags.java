package tgw.evolution.util.constants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@MagicConstant(flagsFromClass = BlockFlags.class)
public @interface BlockFlags {
    /**
     * Calls
     * {@link net.minecraft.world.level.block.Block#neighborChanged(BlockState, Level, BlockPos, Block, BlockPos, boolean)} on surrounding blocks
     * (with isMoving as false). Also updates comparator output state.
     */
    int NOTIFY = 1;
    /**
     * Calls {@link Level#sendBlockUpdated(BlockPos, BlockState, BlockState, int)}.<br>
     * Server-side, this updates all the path-finding navigators.
     */
    int BLOCK_UPDATE = 2;
    /**
     * Stops the blocks from being marked for a render update
     */
    int NO_RERENDER = 4;
    /**
     * Makes the block be re-rendered immediately, on the main thread.
     * If NO_RERENDER is set, then this will be ignored
     */
    int RERENDER = 8;
    /**
     * Causes neighbor updates to be sent to all surrounding blocks (including
     * diagonals).
     */
    int UPDATE_NEIGHBORS = 16;
    /**
     * Prevents neighbor changes from spawning item drops, used by
     * {@link Block#updateOrDestroy(BlockState, BlockState, LevelAccessor, BlockPos, int)}.
     */
    int NO_NEIGHBOR_DROPS = 32;
    /**
     * Tell the block being changed that it was moved, rather than removed/replaced,
     * the boolean value is eventually passed to
     * {@link Block#onRemove(BlockState, Level, BlockPos, BlockState, boolean)}
     * as the last parameter.
     */
    int IS_MOVING = 64;
}
