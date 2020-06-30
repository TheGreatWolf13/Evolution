package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import tgw.evolution.blocks.BlockLogPile;
import tgw.evolution.util.EnumWoodVariant;

public class ItemLog extends ItemBlockPlaceable {

    public EnumWoodVariant variant;

    public ItemLog(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public BlockState getSneakingState(BlockItemUseContext context) {
        return this.variant.getPile().getStateForPlacement(context);
    }

    @Override
    public boolean customCondition(Block block) {
        return block instanceof BlockLogPile;
    }

    @Override
    public BlockState getCustomState(BlockItemUseContext context) {
        return this.variant.getPile().getStateForPlacement(context);
    }
}
