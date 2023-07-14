package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchBlockAndTintGetter;

@Mixin(BlockAndTintGetter.class)
public interface MixinBlockAndTintGetter extends PatchBlockAndTintGetter {

    @Shadow
    LevelLightEngine getLightEngine();

    /**
     * @reason Deprecated
     */
    @Overwrite
    default int getRawBrightness(BlockPos pos, int subtractedSkyLight) {
        Evolution.deprecatedMethod();
        return this.getRawBrightness_(pos.asLong(), subtractedSkyLight);
    }

    @Override
    default int getRawBrightness_(long pos, int subtractedSkyLight) {
        return this.getLightEngine().getRawBrightness_(pos, subtractedSkyLight);
    }
}
