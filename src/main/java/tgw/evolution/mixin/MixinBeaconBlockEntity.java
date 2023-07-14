package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeaconBlockEntity.class)
public abstract class MixinBeaconBlockEntity extends BlockEntity {

    public MixinBeaconBlockEntity(BlockEntityType<?> blockEntityType,
                                  BlockPos blockPos,
                                  BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}
