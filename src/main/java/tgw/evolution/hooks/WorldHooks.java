package tgw.evolution.hooks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tgw.evolution.blocks.IFluidLoggable;

public final class WorldHooks {

    private WorldHooks() {
    }

    /**
     * Hooks from {@link World#removeBlock(BlockPos, boolean)}
     */
    @EvolutionHook
    public static boolean removeBlock(World world, BlockPos pos, boolean isMoving) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable) {
            if (((IFluidLoggable) block).remove(world, pos, state)) {
                return true;
            }
        }
        IFluidState fluidState = world.getFluidState(pos);
        return world.setBlockState(pos, fluidState.getBlockState(), 3 | (isMoving ? 64 : 0));
    }
}
