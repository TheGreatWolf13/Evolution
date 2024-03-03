package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CommandBlockEntity.class)
public abstract class MixinCommandBlockEntity extends BlockEntity {

    public MixinCommandBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean isConditional() {
        assert this.level != null;
        BlockState state = this.level.getBlockState_(this.getBlockPos());
        return state.getBlock() instanceof CommandBlock ? state.getValue(CommandBlock.CONDITIONAL) : false;
    }
}
