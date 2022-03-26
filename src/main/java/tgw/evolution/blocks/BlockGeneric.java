package tgw.evolution.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.patches.IBlockPatch;
import tgw.evolution.util.constants.HarvestLevels;

public abstract class BlockGeneric extends Block implements IBlockPatch {

    public BlockGeneric(Properties properties) {
        super(properties);
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return HarvestLevels.HAND;
    }
}
