package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IReplaceable {

    boolean canBeReplacedByFluid(BlockState state);

    boolean canBeReplacedByRope(BlockState state);

    NonNullList<ItemStack> getDrops(Level level, BlockPos pos, BlockState state);

    boolean isReplaceable(BlockState state);

    default void onReplaced(BlockState state, Level level, BlockPos pos) {
        for (ItemStack stack : this.getDrops(level, pos, state)) {
            BlockUtils.dropItemStack(level, pos, stack);
        }
    }
}
