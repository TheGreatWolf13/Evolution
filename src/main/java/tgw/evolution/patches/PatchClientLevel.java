package tgw.evolution.patches;

import net.minecraft.world.level.block.state.BlockState;

public interface PatchClientLevel {

    default void setKnownState_(int x, int y, int z, BlockState state) {
        throw new AbstractMethodError();
    }
}
