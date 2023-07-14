package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchLevelLightEngine;

@Mixin(LevelLightEngine.class)
public abstract class MixinLevelLightEngine implements PatchLevelLightEngine {

    @Shadow @Final private @Nullable LayerLightEngine<?, ?> blockEngine;
    @Shadow @Final private @Nullable LayerLightEngine<?, ?> skyEngine;

    @Overwrite
    public int getRawBrightness(BlockPos pos, int reducedSkyLight) {
        Evolution.deprecatedMethod();
        return this.getRawBrightness_(pos.asLong(), reducedSkyLight);
    }

    @Override
    public int getRawBrightness_(long pos, int reducedSkyLight) {
        int skyLight = this.skyEngine == null ? 0 : this.skyEngine.storage.getLightValue(pos) - reducedSkyLight;
        int blockLight = this.blockEngine == null ? 0 : this.blockEngine.storage.getLightValue(pos);
        return Math.max(blockLight, skyLight);
    }
}
