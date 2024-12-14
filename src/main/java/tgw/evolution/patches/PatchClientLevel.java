package tgw.evolution.patches;

import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.state.BlockState;

public interface PatchClientLevel {

    default int calculateBlockTint_(int x, int y, int z, ColorResolver colorResolver) {
        throw new AbstractMethodError();
    }

    default void onChunkLoaded(int chunkX, int chunkZ) {
        throw new AbstractMethodError();
    }

    default void onSectionBecomingNonEmpty(long secPos) {
        throw new AbstractMethodError();
    }

    default void setKnownState_(int x, int y, int z, BlockState state) {
        throw new AbstractMethodError();
    }
}
