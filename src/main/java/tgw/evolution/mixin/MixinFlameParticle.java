package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.math.AABBMutable;

@Mixin(FlameParticle.class)
public abstract class MixinFlameParticle extends RisingParticle {

    public MixinFlameParticle(ClientLevel p_107631_,
                              double p_107632_,
                              double p_107633_,
                              double p_107634_,
                              double p_107635_, double p_107636_, double p_107637_) {
        super(p_107631_, p_107632_, p_107633_, p_107634_, p_107635_, p_107636_, p_107637_);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @Override
    public int getLightColor(float partialTicks) {
        float life = (this.age + partialTicks) / this.lifetime;
        life = Mth.clamp(life, 0.0F, 1.0F);
        int color = super.getLightColor(partialTicks);
        int bl = color & 0xF;
        int sl = color >> 16 & 0xF;
        bl += (int) (life * 15);
        if (bl > 15) {
            bl = 15;
        }
        return bl | bl << 4 | bl << 20 | sl << 16;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Override
    @Overwrite
    public void move(double x, double y, double z) {
        ((AABBMutable) this.getBoundingBox()).moveMutable(x, y, z);
        this.setLocationFromBoundingbox();
    }
}
