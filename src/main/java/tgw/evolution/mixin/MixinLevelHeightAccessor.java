package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;

@Mixin(LevelHeightAccessor.class)
public interface MixinLevelHeightAccessor {

    @Shadow
    boolean isOutsideBuildHeight(int i);

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    default boolean isOutsideBuildHeight(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isOutsideBuildHeight(pos.getY());
    }
}
