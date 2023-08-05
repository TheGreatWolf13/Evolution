package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.world.util.LevelUtils;

import java.util.Random;

@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class Mixin_M_SpreadingSnowyDirtBlock extends SnowyDirtBlock {

    public Mixin_M_SpreadingSnowyDirtBlock(Properties properties) {
        super(properties);
    }

    @Overwrite
    @DeleteMethod
    private static boolean canBeGrass(BlockState state, LevelReader level, BlockPos pos) {
        throw new AbstractMethodError();
    }

    @Unique
    private static boolean canBeGrass_(BlockState state, BlockGetter level, int x, int y, int z) {
        BlockState stateAbove = level.getBlockState_(x, y + 1, z);
        if (stateAbove.is(Blocks.SNOW) && stateAbove.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        if (stateAbove.getFluidState().getAmount() == 8) {
            return false;
        }
        int lightBlock = LevelUtils.getLightBlockInto(level, state, x, y, z, stateAbove, x, y + 1, z, Direction.UP,
                                                      stateAbove.getLightBlock_(level, x, y + 1, z));
        return lightBlock < level.getMaxLightLevel();
    }

    @Overwrite
    @DeleteMethod
    private static boolean canPropagate(BlockState state, LevelReader level, BlockPos pos) {
        throw new AbstractMethodError();
    }

    @Unique
    private static boolean canPropagate_(BlockState state, BlockGetter level, int x, int y, int z) {
        return canBeGrass_(state, level, x, y, z) && !level.getFluidState_(x, y + 1, z).is(FluidTags.WATER);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (!canBeGrass_(state, level, x, y, z)) {
            level.setBlockAndUpdate_(x, y, z, Blocks.DIRT.defaultBlockState());
        }
        else {
            if (level.getMaxLocalRawBrightness_(x, y + 1, z) >= 9) {
                BlockState defaultState = this.defaultBlockState();
                for (int i = 0; i < 4; ++i) {
                    int offX = x + random.nextInt(3) - 1;
                    int offY = y + random.nextInt(5) - 3;
                    int offZ = z + random.nextInt(3) - 1;
                    if (level.getBlockState_(offX, offY, offZ).is(Blocks.DIRT)) {
                        if (canPropagate_(defaultState, level, offX, offY, offZ)) {
                            level.setBlockAndUpdate_(offX, offY, offZ,
                                                     defaultState.setValue(SNOWY, level.getBlockState_(offX, offY + 1, offZ).is(Blocks.SNOW)));
                        }
                    }
                }
            }
        }
    }
}
