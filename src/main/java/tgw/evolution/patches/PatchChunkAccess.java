package tgw.evolution.patches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.LList;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.maps.R2OMap;
import tgw.evolution.world.lighting.SWMRNibbleArray;
import tgw.evolution.world.lighting.SWMRShortArray;

public interface PatchChunkAccess {

    default L2OMap<BlockEntity> blockEntities_() {
        throw new AbstractMethodError();
    }

    default boolean @Nullable [] getBlockEmptinessMap() {
        throw new AbstractMethodError();
    }

    default @Nullable CompoundTag getBlockEntityNbtForSaving_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default @Nullable CompoundTag getBlockEntityNbt_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default SWMRShortArray[] getBlockShorts() {
        throw new AbstractMethodError();
    }

    default LList getLights_() {
        throw new AbstractMethodError();
    }

    default boolean @Nullable [] getSkyEmptinessMap() {
        throw new AbstractMethodError();
    }

    default SWMRNibbleArray[] getSkyNibbles() {
        throw new AbstractMethodError();
    }

    default R2OMap<Heightmap.Types, Heightmap> heightmaps_() {
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

    default void setBlockEmptinessMap(boolean @Nullable [] emptinessMap) {
        throw new AbstractMethodError();
    }

    default void setBlockShorts(SWMRShortArray[] shorts) {
        throw new AbstractMethodError();
    }

    default @Nullable BlockState setBlockState_(int x, int y, int z, BlockState state, boolean isMoving) {
        throw new AbstractMethodError();
    }

    default void setSkyEmptinessMap(boolean @Nullable [] emptinessMap) {
        throw new AbstractMethodError();
    }

    default void setSkyNibbles(SWMRNibbleArray[] nibbles) {
        throw new AbstractMethodError();
    }
}
