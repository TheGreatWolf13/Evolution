package tgw.evolution.patches;

import net.minecraft.world.level.BlockGetter;

public interface PatchFluidState {

    default float getHeight_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
