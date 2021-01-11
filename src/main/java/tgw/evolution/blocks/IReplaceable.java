package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IReplaceable {

    boolean canBeReplacedByFluid(BlockState state);

    boolean canBeReplacedByRope(BlockState state);

    ItemStack getDrops(World world, BlockPos pos, BlockState state);

    boolean isReplaceable(BlockState state);

    default void onReplaced(BlockState state, World world, BlockPos pos) {
        BlockUtils.dropItemStack(world, pos, this.getDrops(world, pos, state));
    }
}
