package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchLevelAccessor;
import tgw.evolution.util.constants.LvlEvent;

@Mixin(LevelAccessor.class)
public interface MixinLevelAccessor extends PatchLevelAccessor {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default void blockUpdated(BlockPos pos, Block block) {
        Evolution.deprecatedMethod();
        this.blockUpdated_(pos.getX(), pos.getY(), pos.getZ(), block);
    }

    @Override
    default void blockUpdated_(int x, int y, int z, Block block) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default void levelEvent(@LvlEvent int event, BlockPos pos, int data) {
        Evolution.deprecatedMethod();
        this.levelEvent_(event, pos.getX(), pos.getY(), pos.getZ(), data);
    }
}
