package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.EnchantmentTableParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.util.math.AABBMutable;

@Mixin(EnchantmentTableParticle.class)
public abstract class MixinEnchantmentTableParticle extends TextureSheetParticle {

    @Shadow @Final private double xStart;
    @Shadow @Final private double yStart;
    @Shadow @Final private double zStart;

    public MixinEnchantmentTableParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Override
    @Overwrite
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

    @Override
    @Overwrite
    public void move(double dx, double dy, double dz) {
        ((AABBMutable) this.getBoundingBox()).moveMutable(dx, dy, dz);
        this.setLocationFromBoundingbox();
    }

    @Override
    @Overwrite
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        else {
            float f = this.age / (float) this.lifetime;
            f = 1.0F - f;
            float g = 1.0F - f;
            g *= g;
            g *= g;
            this.setPos(this.xStart + this.xd * f, this.yStart + this.yd * f - g * 1.2, this.zStart + this.zd * f);
        }
    }
}
