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
        int increment = (int) (life * 15);
        int rr = color & 15;
        int rs = color & 1 << 4;
        int gr = color >> 5 & 15;
        int gs = color & 1 << 9;
        int br = color >> 20 & 15;
        int bs = color & 1 << 24;
        int s = color >> 16 & 0xF;
        rr += increment;
        if (rr > 15) {
            rr = 15;
        }
        gr += increment;
        if (gr > 15) {
            gr = 15;
        }
        br += increment;
        if (br > 15) {
            br = 15;
        }
        return rr | rs | gr << 5 | gs | br << 20 | bs | s << 16;
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
