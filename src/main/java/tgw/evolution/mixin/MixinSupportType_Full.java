package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchSupportType;

@Mixin(targets = "net.minecraft.world.level.block.SupportType$1")
public abstract class MixinSupportType_Full implements PatchSupportType {

    @Overwrite
    public boolean isSupporting(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        Evolution.deprecatedMethod();
        return this.isSupporting_(state, level, pos.getX(), pos.getY(), pos.getZ(), direction);
    }

    @Override
    public boolean isSupporting_(BlockState state, BlockGetter level, int x, int y, int z, Direction direction) {
        return Block.isFaceFull(state.getBlockSupportShape_(level, x, y, z), direction);
    }
}
