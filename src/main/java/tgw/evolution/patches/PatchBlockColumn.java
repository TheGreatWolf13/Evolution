package tgw.evolution.patches;

import net.minecraft.world.level.block.state.BlockState;

public interface PatchBlockColumn {

    default BlockState getBlock_(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
