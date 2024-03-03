package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GlowParticle.class)
public abstract class MixinGlowParticle extends TextureSheetParticle {

    public MixinGlowParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public int getLightColor(float partialTicks) {
        float life = Mth.clamp((this.age + partialTicks) / this.lifetime, 0, 1);
        int color = super.getLightColor(partialTicks);
        int bl = color & 0xF;
        int sl = color >> 16 & 0xF;
        bl += (int) (life * 15);
        if (bl > 15) {
            bl = 15;
        }
        return bl | bl << 4 | bl << 20 | sl << 16;
    }
}
