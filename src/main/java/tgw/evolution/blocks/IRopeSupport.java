package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public interface IRopeSupport {

    int getRopeLength();

    boolean canSupport(BlockState state, Direction direction);
}
