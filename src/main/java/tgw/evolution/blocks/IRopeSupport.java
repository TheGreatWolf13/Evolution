package tgw.evolution.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IRopeSupport {

    boolean canSupport(BlockState state, Direction direction);

    int getRopeLength();
}
