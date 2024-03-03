package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BubbleParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BubbleParticle.class)
public abstract class MixinBubbleParticle extends TextureSheetParticle {

    public MixinBubbleParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        }
        else {
            this.yd += 0.002;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.85;
            this.yd *= 0.85;
            this.zd *= 0.85;
            if (!this.level.getFluidState_(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z)).is(FluidTags.WATER)) {
                this.remove();
            }
        }
    }
}
