package tgw.evolution.blocks;

import net.minecraft.world.level.block.state.BlockState;

public interface IReplaceable {

    boolean canBeReplacedByFluid(BlockState state);

    boolean canBeReplacedByRope(BlockState state);

    boolean isReplaceable(BlockState state);
}
