package tgw.evolution.patches;

import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface PatchModelBlockRendererCache {

    default int getLightColor_(BlockState state, BlockAndTintGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default float getShadeBrightness_(BlockState state, BlockAndTintGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
