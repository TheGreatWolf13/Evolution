package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;

@Mixin(LayerLightEventListener.DummyLightLayerEventListener.class)
public abstract class MixinDummyLightLayerEventListener implements LayerLightEventListener {

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
}
