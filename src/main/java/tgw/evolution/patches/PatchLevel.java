package tgw.evolution.patches;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.LvlEvent;

public interface PatchLevel extends PatchLevelWriter {

    static boolean isInSpawnableBounds_(int x, int y, int z) {
        return !Level.isOutsideSpawnableHeight(y) && x >= -30_000_000 && z >= -30_000_000 && x < 30_000_000 && z < 30_000_000;
    }

    default void addDestroyBlockEffect_(int x, int y, int z, BlockState state) {
        throw new AbstractMethodError();
    }

    default void blockEntityChanged_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void destroyBlockProgress(int breakerId, long pos, int progress, @Nullable Direction face, double hitX, double hitY, double hitZ) {
        throw new AbstractMethodError();
    }

    /**
     * Returns the chunk at any stage, if it exists, immediately.
     */
    default @Nullable ChunkAccess getAnyChunkImmediately(int chunkX, int chunkZ) {
        throw new AbstractMethodError();
    }

    /**
     * Returns the full chunk without blocking.
     */
    default @Nullable LevelChunk getChunkAtImmediately(int chunkX, int chunkZ) {
        throw new AbstractMethodError();
    }

    default LevelChunk getChunkAt_(int x, int z) {
        throw new AbstractMethodError();
    }

    default int getDirectSignalTo_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default int getSignal_(int x, int y, int z, Direction dir) {
        throw new AbstractMethodError();
    }

    default void globalLevelEvent_(@LvlEvent int event, int x, int y, int z, int data) {
        throw new AbstractMethodError();
    }

    default boolean hasNeighborSignal_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean isHumidAt_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean isInWorldBounds_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean isLoaded_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void neighborChanged_(int x, int y, int z, Block block, int fromX, int fromY, int fromZ) {
        throw new AbstractMethodError();
    }

    default void onBlockStateChange_(int x, int y, int z, BlockState oldState, BlockState newState) {
        throw new AbstractMethodError();
    }

    default void removeBlockEntity_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void sendBlockUpdated_(int x, int y, int z, BlockState oldState, BlockState newState, @BlockFlags int flags) {
        throw new AbstractMethodError();
    }

    @Override
    default boolean setBlockAndUpdate_(int x, int y, int z, BlockState state) {
        throw new AbstractMethodError();
    }

    default void setBlocksDirty_(int x, int y, int z, BlockState oldState, BlockState newState) {
        throw new AbstractMethodError();
    }

    default void updateNeighborsAt_(int x, int y, int z, Block block) {
        throw new AbstractMethodError();
    }

    default void updateNeighbourForOutputSignal_(int x, int y, int z, Block block) {
        throw new AbstractMethodError();
    }
}
