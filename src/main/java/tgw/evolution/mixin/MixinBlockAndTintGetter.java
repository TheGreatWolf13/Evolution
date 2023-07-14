package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchBlockAndTintGetter;

@Mixin(BlockAndTintGetter.class)
public interface MixinBlockAndTintGetter extends PatchBlockAndTintGetter {

    @Overwrite
    default int getBrightness(LightLayer lightLayer, BlockPos pos) {
        return this.getBrightness_(lightLayer, pos.asLong());
    }

    @Override
    default int getBrightness_(LightLayer lightLayer, long pos) {
        return this.getLightEngine().getLayerListener(lightLayer).getLightValue_(pos);
    }

    @Shadow
    LevelLightEngine getLightEngine();

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
