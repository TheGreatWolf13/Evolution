package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchSupportType;

@Mixin(targets = "net.minecraft.world.level.block.SupportType$1")
public abstract class MixinSupportType_Full implements PatchSupportType {

    @Override
    public boolean isSupporting_(BlockState state, BlockGetter level, int x, int y, int z, Direction direction) {
        return Block.isFaceFull(state.getBlockSupportShape_(level, x, y, z), direction);
    }
}
