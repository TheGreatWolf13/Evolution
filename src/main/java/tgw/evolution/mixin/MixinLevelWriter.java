package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchLevelWriter;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(LevelWriter.class)
public interface MixinLevelWriter extends PatchLevelWriter {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default boolean setBlock(BlockPos pos, BlockState state, @BlockFlags int flags) {
        Evolution.deprecatedMethod();
        return this.setBlock_(pos.getX(), pos.getY(), pos.getZ(), state, flags);
    }
}
