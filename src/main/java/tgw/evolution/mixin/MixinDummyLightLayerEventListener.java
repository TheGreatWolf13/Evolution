package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;

@Mixin(LayerLightEventListener.DummyLightLayerEventListener.class)
public abstract class MixinDummyLightLayerEventListener implements LayerLightEventListener {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void checkBlock(BlockPos pos) {
        Evolution.deprecatedMethod();
    }

    @Override
    public void checkBlock_(long pos) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void enableLightSources(ChunkPos chunkPos, boolean bl) {
        Evolution.deprecatedMethod();
    }

    @Override
    public void enableLightSources_(int secX, int secZ, boolean bl) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public int getLightValue(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getLightValue_(pos.asLong());
    }

    @Override
    public int getLightValue_(long pos) {
        return 0;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void onBlockEmissionIncrease(BlockPos pos, int lightEmission) {
        Evolution.deprecatedMethod();
        this.onBlockEmissionIncrease_(pos.asLong(), lightEmission);
    }

    @Override
    public void onBlockEmissionIncrease_(long pos, int lightEmission) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void updateSectionStatus(SectionPos secPos, boolean hasOnlyAir) {
        Evolution.deprecatedMethod();
    }

    @Override
    public void updateSectionStatus_sec(int secX, int secY, int secZ, boolean hasOnlyAir) {
    }
}
