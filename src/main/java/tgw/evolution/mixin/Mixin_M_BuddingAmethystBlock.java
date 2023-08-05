package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

@Mixin(BuddingAmethystBlock.class)
public abstract class Mixin_M_BuddingAmethystBlock extends AmethystBlock {

    public Mixin_M_BuddingAmethystBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public static boolean canClusterGrowAtState(BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (random.nextInt(5) == 0) {
            Direction direction = DirectionUtil.ALL[random.nextInt(6)];
            BlockPos blockPos2 = new BlockPos(x, y, z).relative(direction);
            BlockState blockState2 = level.getBlockState_(blockPos2);
            Block block = null;
            if (canClusterGrowAtState(blockState2)) {
                block = Blocks.SMALL_AMETHYST_BUD;
            }
            else if (blockState2.is(Blocks.SMALL_AMETHYST_BUD) && blockState2.getValue(AmethystClusterBlock.FACING) == direction) {
                block = Blocks.MEDIUM_AMETHYST_BUD;
            }
            else if (blockState2.is(Blocks.MEDIUM_AMETHYST_BUD) && blockState2.getValue(AmethystClusterBlock.FACING) == direction) {
                block = Blocks.LARGE_AMETHYST_BUD;
            }
            else if (blockState2.is(Blocks.LARGE_AMETHYST_BUD) && blockState2.getValue(AmethystClusterBlock.FACING) == direction) {
                block = Blocks.AMETHYST_CLUSTER;
            }
            if (block != null) {
                BlockState blockState3 = block.defaultBlockState()
                                              .setValue(AmethystClusterBlock.FACING, direction).setValue(
                                AmethystClusterBlock.WATERLOGGED, blockState2.getFluidState().getType() == Fluids.WATER);
                level.setBlockAndUpdate_(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ(), blockState3);
            }

        }
    }
}
