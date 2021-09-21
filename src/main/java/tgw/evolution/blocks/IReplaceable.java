package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public interface IReplaceable {

    boolean canBeReplacedByFluid(BlockState state);

    boolean canBeReplacedByRope(BlockState state);

    @Nonnull
    NonNullList<ItemStack> getDrops(World world, BlockPos pos, BlockState state);

    boolean isReplaceable(BlockState state);

    default void onReplaced(BlockState state, World world, BlockPos pos) {
        for (ItemStack stack : this.getDrops(world, pos, state)) {
            BlockUtils.dropItemStack(world, pos, stack);
        }
    }
}
