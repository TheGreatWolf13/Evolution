package tgw.evolution.patches;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface PatchParticleEngine {

    default void crack_(int x, int y, int z, Direction face, double hitX, double hitY, double hitZ) {
        throw new AbstractMethodError();
    }

    default void destroy_(int x, int y, int z, BlockState state) {
        throw new AbstractMethodError();
    }

    int getRenderedParticles();
}
