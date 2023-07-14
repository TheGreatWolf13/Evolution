package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LayerLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class MixinSpreadingSnowyDirtBlock extends SnowyDirtBlock {

    public MixinSpreadingSnowyDirtBlock(Properties properties) {
        super(properties);
    }

    @Overwrite
    private static boolean canBeGrass(BlockState state, LevelReader level, BlockPos pos) {
        BlockState stateAbove = level.getBlockState_(pos.getX(), pos.getY() + 1, pos.getZ());
        if (stateAbove.is(Blocks.SNOW) && stateAbove.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        if (stateAbove.getFluidState().getAmount() == 8) {
            return false;
        }
        BlockPos above = pos.above();
        int i = LayerLightEngine.getLightBlockInto(level, state, pos, stateAbove, above, Direction.UP,
                                                   stateAbove.getLightBlock(level, above));
        return i < level.getMaxLightLevel();
    }

    @Overwrite
    private static boolean canPropagate(BlockState state, LevelReader level, BlockPos pos) {
        return canBeGrass(state, level, pos) && !level.getFluidState_(pos.getX(), pos.getY() + 1, pos.getZ()).is(FluidTags.WATER);
    }

    @Override
    @Overwrite
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (!canBeGrass(state, level, pos)) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        }
        else {
            if (level.getMaxLocalRawBrightness_(pos.getX(), pos.getY() + 1, pos.getZ()) >= 9) {
                BlockState defaultState = this.defaultBlockState();
                for (int i = 0; i < 4; ++i) {
                    int x = pos.getX() + random.nextInt(3) - 1;
                    int y = pos.getY() + random.nextInt(5) - 3;
                    int z = pos.getZ() + random.nextInt(3) - 1;
                    if (level.getBlockState_(x, y, z).is(Blocks.DIRT)) {
                        //At least the allocation is behind a check, instead of simply allocating 4 BlockPos
                        BlockPos randomPos = new BlockPos(x, y, z);
                        if (canPropagate(defaultState, level, randomPos)) {
                            level.setBlockAndUpdate(randomPos, defaultState.setValue(SNOWY, level.getBlockState_(x, y + 1, z).is(Blocks.SNOW)));
                        }
                    }
                }
            }
        }
    }
}
