package tgw.evolution.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.blocks.IFriction;

@Mixin(Block.class)
public abstract class BlockMixin extends AbstractBlock implements IFriction {

    public BlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.85f;
    }
}
