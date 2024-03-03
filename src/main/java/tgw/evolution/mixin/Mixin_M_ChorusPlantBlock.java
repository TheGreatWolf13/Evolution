package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

@Mixin(ChorusPlantBlock.class)
public abstract class Mixin_M_ChorusPlantBlock extends PipeBlock {

    public Mixin_M_ChorusPlantBlock(float f, Properties properties) {
        super(f, properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        BlockState stateBelow = level.getBlockState_(x, y - 1, z);
        boolean notAirAboveAndBelow = !level.getBlockState_(x, y + 1, z).isAir() && !stateBelow.isAir();
        for (Direction direction : DirectionUtil.HORIZ_NESW) {
            BlockState stateAtSide = level.getBlockStateAtSide(x, y, z, direction);
            if (stateAtSide.is(this)) {
                if (notAirAboveAndBelow) {
                    return false;
                }
                BlockState stateAtSideBelow = level.getBlockStateAtSide(x, y - 1, z, direction);
                if (stateAtSideBelow.is(this) || stateAtSideBelow.is(Blocks.END_STONE)) {
                    return true;
                }
            }
        }
        return stateBelow.is(this) || stateBelow.is(Blocks.END_STONE);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (!state.canSurvive_(level, x, y, z)) {
            level.destroyBlock_(x, y, z, true);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public BlockState updateShape(BlockState blockState,
                                  Direction direction,
                                  BlockState blockState2,
                                  LevelAccessor levelAccessor,
                                  BlockPos blockPos,
                                  BlockPos blockPos2) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState updateShape_(BlockState state,
                                   Direction from,
                                   BlockState fromState,
                                   LevelAccessor level,
                                   int x,
                                   int y,
                                   int z,
                                   int fromX,
                                   int fromY,
                                   int fromZ) {
        if (!state.canSurvive_(level, x, y, z)) {
            level.scheduleTick(new BlockPos(x, y, z), this, 1);
            return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
        }
        boolean bl = fromState.is(this) ||
                     fromState.is(Blocks.CHORUS_FLOWER) ||
                     from == Direction.DOWN && fromState.is(Blocks.END_STONE);
        return state.setValue(PROPERTY_BY_DIRECTION.get(from), bl);
    }
}
