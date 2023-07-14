package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.math.AABBMutable;

@Mixin(SuspendedTownParticle.class)
public abstract class MixinSuspendedTownParticle extends TextureSheetParticle {

    public MixinSuspendedTownParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
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
