package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEventListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchLevelLightEngine;

@Mixin(LevelLightEngine.class)
public abstract class MixinLevelLightEngine implements PatchLevelLightEngine, LightEventListener {

    @Shadow @Final private @Nullable LayerLightEngine<?, ?> blockEngine;
    @Shadow @Final private @Nullable LayerLightEngine<?, ?> skyEngine;

    @Override
    @Overwrite
    public void checkBlock(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.checkBlock_(pos.asLong());
    }

    @Override
    public void checkBlock_(long pos) {
        if (this.blockEngine != null) {
            this.blockEngine.checkBlock_(pos);
        }
        if (this.skyEngine != null) {
            this.skyEngine.checkBlock_(pos);
        }
    }

    @Override
    @Overwrite
    public void enableLightSources(ChunkPos chunkPos, boolean bl) {
        Evolution.deprecatedMethod();
        this.enableLightSources_(chunkPos.x, chunkPos.z, bl);
    }

    @Override
    public void enableLightSources_(int secX, int secZ, boolean bl) {
        if (this.blockEngine != null) {
            this.blockEngine.enableLightSources_(secX, secZ, bl);
        }
        if (this.skyEngine != null) {
            this.skyEngine.enableLightSources_(secX, secZ, bl);
        }
    }

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

    @Override
    @Overwrite
    public void onBlockEmissionIncrease(BlockPos pos, int lightEmission) {
        Evolution.deprecatedMethod();
        this.onBlockEmissionIncrease_(pos.asLong(), lightEmission);
    }

    @Override
    public void onBlockEmissionIncrease_(long pos, int lightEmission) {
        if (this.blockEngine != null) {
            this.blockEngine.onBlockEmissionIncrease_(pos, lightEmission);
        }
    }

    @Overwrite
    public void queueSectionData(LightLayer lightLayer, SectionPos secPos, @Nullable DataLayer dataLayer, boolean bl) {
        Evolution.deprecatedMethod();
        this.queueSectionData_(lightLayer, secPos.x(), secPos.y(), secPos.z(), dataLayer, bl);
    }

    @Override
    public void queueSectionData_(LightLayer lightLayer, int secX, int secY, int secZ, @Nullable DataLayer dataLayer, boolean bl) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                this.blockEngine.queueSectionData(SectionPos.asLong(secX, secY, secZ), dataLayer, bl);
            }
        }
        else if (this.skyEngine != null) {
            this.skyEngine.queueSectionData(SectionPos.asLong(secX, secY, secZ), dataLayer, bl);
        }
    }

    @Override
    @Overwrite
    public void updateSectionStatus(SectionPos secPos, boolean hasOnlyAir) {
        Evolution.deprecatedMethod();
        this.updateSectionStatus_sec(secPos.x(), secPos.y(), secPos.z(), hasOnlyAir);
    }

    @Override
    public void updateSectionStatus_sec(int secX, int secY, int secZ, boolean hasOnlyAir) {
        if (this.blockEngine != null) {
            this.blockEngine.updateSectionStatus_sec(secX, secY, secZ, hasOnlyAir);
        }
        if (this.skyEngine != null) {
            this.skyEngine.updateSectionStatus_sec(secX, secY, secZ, hasOnlyAir);
        }
    }
}
