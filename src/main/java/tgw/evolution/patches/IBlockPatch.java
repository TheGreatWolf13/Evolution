package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.constants.HarvestLevel;

public interface IBlockPatch {

    /**
     * Common Values: <br>
     * 0.55 = Sand; <br>
     * 0.57 = Gravel; <br>
     * 0.60 = Clay, Glass, Grass; <br>
     * 0.61 = Dry Grass; <br>
     * 0.63 = Dirt; <br>
     * 0.70 = Wood; <br>
     * 0.80 = Stone; <br>
     */
    float getFrictionCoefficient(BlockState state);

    @HarvestLevel
    int getHarvestLevel(BlockState state, @Nullable Level level, @Nullable BlockPos pos);

    default boolean shouldCull(BlockGetter level, BlockState state, BlockPos pos, BlockState adjacentState, BlockPos adjacentPos, Direction face) {
        return state.skipRendering(adjacentState, face);
    }
}
