package tgw.evolution.patches;

import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.util.constants.HarvestLevel;

public interface IBlockPatch {

    float getFrictionCoefficient(BlockState state);

    @HarvestLevel
    int getHarvestLevel(BlockState state);
}
