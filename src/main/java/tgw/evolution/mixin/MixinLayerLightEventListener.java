package tgw.evolution.mixin;

import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LightEventListener;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchLayerLightEventListener;

@Mixin(LayerLightEventListener.class)
public interface MixinLayerLightEventListener extends PatchLayerLightEventListener, LightEventListener {

    @Override
    int getLightValue_(long pos);
}
