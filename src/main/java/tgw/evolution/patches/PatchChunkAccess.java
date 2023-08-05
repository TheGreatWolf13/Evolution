package tgw.evolution.patches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.LList;
import tgw.evolution.util.collection.maps.L2OMap;

import java.util.Map;

public interface PatchChunkAccess {

    default L2OMap<BlockEntity> blockEntities_() {
        throw new AbstractMethodError();
    }

    default @Nullable CompoundTag getBlockEntityNbtForSaving_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default @Nullable CompoundTag getBlockEntityNbt_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default LList getLights_() {
        throw new AbstractMethodError();
    }

    default Map<Heightmap.Types, Heightmap> heightmaps_() {
        throw new AbstractMethodError();
    }

    default void markPosForPostprocessing_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default L2OMap<CompoundTag> pendingBlockEntities_() {
        throw new AbstractMethodError();
    }

    default void removeBlockEntity_(long pos) {
        throw new AbstractMethodError();
    }

    default @Nullable BlockState setBlockState_(int x, int y, int z, BlockState state, boolean isMoving) {
        throw new AbstractMethodError();
    }
}
