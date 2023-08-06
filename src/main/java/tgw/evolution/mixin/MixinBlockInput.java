package tgw.evolution.mixin;

import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.util.constants.BlockFlags;

import java.util.function.Predicate;

@Mixin(BlockInput.class)
public abstract class MixinBlockInput implements Predicate<BlockInWorld> {

    @Shadow @Final private BlockState state;

    @Shadow @Final private @Nullable CompoundTag tag;

    @Overwrite
    public boolean place(ServerLevel level, BlockPos pos, @BlockFlags int flags) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockState updatedState = BlockUtils.updateFromNeighbourShapes(this.state, level, x, y, z);
        if (updatedState.isAir()) {
            updatedState = this.state;
        }
        if (!level.setBlock_(x, y, z, updatedState, flags)) {
            return false;
        }
        if (this.tag != null) {
            BlockEntity blockEntity = level.getBlockEntity_(x, y, z);
            if (blockEntity != null) {
                blockEntity.load(this.tag);
            }
        }
        return true;
    }
}
