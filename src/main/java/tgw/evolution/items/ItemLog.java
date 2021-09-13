package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import tgw.evolution.blocks.BlockLog;
import tgw.evolution.blocks.BlockLogPile;
import tgw.evolution.util.WoodVariant;

public class ItemLog extends ItemGenericBlockPlaceable {

    public final WoodVariant variant;

    public ItemLog(WoodVariant variant, BlockLog block, Properties builder) {
        super(block, builder);
        this.variant = variant;
    }

    @Override
    public boolean customCondition(Block blockAtPlacing, Block blockClicking) {
        return blockClicking instanceof BlockLogPile;
    }

    @Override
    public BlockState getCustomState(BlockItemUseContext context) {
        return this.variant.getPile().getStateForPlacement(context);
    }

    @Override
    public BlockState getSneakingState(BlockItemUseContext context) {
        return this.variant.getPile().getStateForPlacement(context);
    }
}
