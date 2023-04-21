package tgw.evolution.blocks;

import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.patches.IBlockPatch;

public interface IReplaceable extends IBlockPatch {

    boolean canBeReplacedByFluid(BlockState state);

    boolean canBeReplacedByRope(BlockState state);

    @Override
    default float getFrictionCoefficient(BlockState state) {
        return 0;
    }

    boolean isReplaceable(BlockState state);
}
