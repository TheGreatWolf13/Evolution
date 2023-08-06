package tgw.evolution.patches;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.constants.BlockFlags;

public interface PatchLevelWriter {

    default boolean destroyBlock_(int x, int y, int z, boolean drop) {
        return this.destroyBlock_(x, y, z, drop, null);
    }

    default boolean destroyBlock_(int x, int y, int z, boolean drop, @Nullable Entity entity) {
        return this.destroyBlock_(x, y, z, drop, entity, 512);
    }

    default boolean destroyBlock_(int x, int y, int z, boolean drop, @Nullable Entity entity, int limit) {
        throw new AbstractMethodError();
    }

    default boolean removeBlock_(int x, int y, int z, boolean isMoving) {
        throw new AbstractMethodError();
    }

    default boolean setBlockAndUpdate_(int x, int y, int z, BlockState state) {
        return this.setBlock_(x, y, z, state, BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
    }

    default boolean setBlock_(int x, int y, int z, BlockState state, @BlockFlags int flags, int limit) {
        throw new AbstractMethodError();
    }

    default boolean setBlock_(int x, int y, int z, BlockState state, @BlockFlags int flags) {
        return this.setBlock_(x, y, z, state, flags, 512);
    }
}
