package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.function.Supplier;

@Mixin(TrappedChestBlock.class)
public abstract class Mixin_M_TrappedChestBlock extends ChestBlock {

    public Mixin_M_TrappedChestBlock(Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier) {
        super(properties, supplier);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    @DeleteMethod
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        throw new AbstractMethodError();
    }

    @Override
    public int getSignal_(BlockState state, BlockGetter level, int x, int y, int z, Direction dir) {
        return Mth.clamp(ChestBlockEntity.getOpenCount(level, new BlockPos(x, y, z)), 0, 15);
    }
}
