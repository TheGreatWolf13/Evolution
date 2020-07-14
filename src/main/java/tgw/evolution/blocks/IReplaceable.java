package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IReplaceable {

    ItemStack getDrops(BlockState state);

    boolean isReplaceable(BlockState state);

    boolean canBeReplacedByRope(BlockState state);

    boolean canBeReplacedByLiquid(BlockState state);

    default void onReplaced(BlockState state, World worldIn, BlockPos pos) {
        BlockUtils.dropItemStack(worldIn, pos, this.getDrops(state));
    }
}
