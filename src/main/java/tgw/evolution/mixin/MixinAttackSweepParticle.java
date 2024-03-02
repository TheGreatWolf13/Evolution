package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.AttackSweepParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.client.renderer.ambient.DynamicLights;

@Mixin(AttackSweepParticle.class)
public abstract class MixinAttackSweepParticle extends TextureSheetParticle {

    public MixinAttackSweepParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Override
    @Overwrite
    public int getLightColor(float partialTicks) {
        return DynamicLights.FULL_LIGHTMAP;
    }
}
