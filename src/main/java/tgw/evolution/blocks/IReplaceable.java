package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

public interface IReplaceable {

    ItemStack getDrops(BlockState state);

    boolean isReplaceable(BlockState state);

    boolean canBeReplacedByRope(BlockState state);
}
