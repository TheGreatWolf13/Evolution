package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.RisingParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.math.AABBMutable;

@Mixin(FlameParticle.class)
public abstract class FlameParticleMixin extends RisingParticle {

    public FlameParticleMixin(ClientLevel p_107631_,
                              double p_107632_,
                              double p_107633_,
                              double p_107634_,
                              double p_107635_, double p_107636_, double p_107637_) {
        super(p_107631_, p_107632_, p_107633_, p_107634_, p_107635_, p_107636_, p_107637_);
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
