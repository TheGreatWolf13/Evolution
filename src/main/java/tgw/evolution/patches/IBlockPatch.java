package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.util.constants.HarvestLevel;

public interface IBlockPatch {

    float getFrictionCoefficient(BlockState state);

    @HarvestLevel
    int getHarvestLevel(BlockState state);

    default boolean shouldCull(BlockGetter level, BlockState state, BlockPos pos, BlockState adjacentState, BlockPos adjacentPos, Direction face) {
        return state.skipRendering(adjacentState, face);
    }
}
