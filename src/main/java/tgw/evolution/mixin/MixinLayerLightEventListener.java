package tgw.evolution.mixin;

import net.minecraft.world.level.lighting.LayerLightEventListener;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchLayerLightEventListener;

@Mixin(LayerLightEventListener.class)
public interface MixinLayerLightEventListener extends PatchLayerLightEventListener {

    @Override
    int getLightValue_(long pos);
}
