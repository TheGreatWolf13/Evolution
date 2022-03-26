package tgw.evolution.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.IBlockPatch;
import tgw.evolution.util.constants.HarvestLevels;

@Mixin(Block.class)
public abstract class BlockMixin extends BlockBehaviour implements IBlockPatch {

    public BlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.85f;
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return HarvestLevels.HAND;
    }
}
