package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.ambient.DynamicLights;

@Mixin(DripParticle.class)
public abstract class MixinDripParticle extends TextureSheetParticle {

    @Shadow protected boolean isGlowing;

    public MixinDripParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Override
    @Overwrite
    public int getLightColor(float partialTicks) {
        return this.isGlowing ? DynamicLights.FULL_LIGHTMAP_NO_SKY : super.getLightColor(partialTicks);
    }
}
