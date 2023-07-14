package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChestBlockEntity.class)
public abstract class MixinChestBlockEntity extends RandomizableContainerBlockEntity {

    public MixinChestBlockEntity(BlockEntityType<?> blockEntityType,
                                 BlockPos blockPos,
                                 BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = this.getBlockPos();
        return this._getBBForRendering().setUnchecked(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
    }
}
