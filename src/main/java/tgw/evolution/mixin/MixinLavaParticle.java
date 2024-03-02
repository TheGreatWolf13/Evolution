package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.client.renderer.ambient.DynamicLights;

@Mixin(LavaParticle.class)
public abstract class MixinLavaParticle extends TextureSheetParticle {

    public MixinLavaParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Override
    @Overwrite
    public int getLightColor(float partialTicks) {
        int color = super.getLightColor(partialTicks);
        int sl = color >> 16 & 0xF;
        return DynamicLights.FULL_LIGHTMAP_NO_SKY | sl << 16;
    }
}
