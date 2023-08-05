package tgw.evolution.patches;

import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface PatchSupportType {

    default boolean isSupporting_(BlockState state, BlockGetter level, int x, int y, int z, Direction direction) {
        throw new AbstractMethodError();
    }
}
