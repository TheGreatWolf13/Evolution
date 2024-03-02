package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.client.renderer.ambient.DynamicLights;

@Mixin(PortalParticle.class)
public abstract class MixinPortalParticle extends TextureSheetParticle {

    public MixinPortalParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Overwrite
    @Override
    public int getLightColor(float partialTicks) {
        int color = super.getLightColor(partialTicks);
        float life = this.age / (float) this.lifetime;
        life *= life;
        life *= life;
        int bl = color & DynamicLights.FULL_LIGHTMAP_NO_SKY;
        int sl = color >> 16 & 0xF;
        sl += (int) (life * 15);
        if (sl > 15) {
            sl = 15;
        }
        return bl | sl << 16;
    }
}
