package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(WaterDropParticle.class)
public abstract class MixinWaterDropParticle extends TextureSheetParticle {

    public MixinWaterDropParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

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
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.98;
            this.yd *= 0.98;
            this.zd *= 0.98;
            if (this.onGround) {
                if (Math.random() < 0.5) {
                    this.remove();
                }
                this.xd *= 0.7;
                this.zd *= 0.7;
            }
            int posX = Mth.floor(this.x);
            int posY = Mth.floor(this.y);
            int posZ = Mth.floor(this.z);
            double d = Math.max(this.level.getBlockState_(posX, posY, posZ).getCollisionShape_(this.level, posX, posY, posZ).max(Direction.Axis.Y, this.x - posX, this.z - posZ), this.level.getFluidState_(posX, posY, posZ).getHeight_(this.level, posX, posY, posZ));
            if (d > 0 && this.y < posY + d) {
                this.remove();
            }
        }
    }
}
