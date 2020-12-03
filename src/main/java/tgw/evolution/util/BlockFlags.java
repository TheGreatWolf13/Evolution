package tgw.evolution.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public final class BlockFlags {
    /**
     * Calls
     * {@link Block#neighborChanged(BlockState, World, BlockPos, Block, BlockPos, boolean)
     * neighborChanged} on surrounding blocks (with isMoving as false). Also updates comparator output state.
     */
    public static final int NOTIFY_NEIGHBORS = 1;
    /**
     * Calls {@link World#notifyBlockUpdate(BlockPos, BlockState, BlockState, int)}.<br>
     * Server-side, this updates all the path-finding navigators.
     */
    public static final int BLOCK_UPDATE = 2;
    /**
     * Stops the blocks from being marked for a render update
     */
    public static final int NO_RERENDER = 4;
    /**
     * Makes the block be re-rendered immediately, on the main thread.
     * If NO_RERENDER is set, then this will be ignored
     */
    public static final int RERENDER = 8;
    /**
     * Causes neighbor updates to be sent to all surrounding blocks (including
     * diagonals). This in turn will call
     * {@link Block#updateDiagonalNeighbors(BlockState, IWorld, BlockPos, int)
     * updateDiagonalNeighbors} on both old and new states, and
     * {@link Block#updateNeighbors(BlockState, IWorld, BlockPos, int)
     * updateNeighbors} on the new state.
     */
    public static final int UPDATE_NEIGHBORS = 16;
    /**
     * Prevents neighbor changes from spawning item drops, used by
     * {@link Block#replaceBlock(BlockState, BlockState, IWorld, BlockPos, int)}.
     */
    public static final int NO_NEIGHBOR_DROPS = 32;
    /**
     * Tell the block being changed that it was moved, rather than removed/replaced,
     * the boolean value is eventually passed to
     * {@link Block#onReplaced(BlockState, World, BlockPos, BlockState, boolean)}
     * as the last parameter.
     */
    public static final int IS_MOVING = 64;

    public static final int NOTIFY_AND_UPDATE = NOTIFY_NEIGHBORS | BLOCK_UPDATE;
    public static final int NOTIFY_UPDATE_AND_RERENDER = NOTIFY_AND_UPDATE | RERENDER;

    private BlockFlags() {
    }
}
