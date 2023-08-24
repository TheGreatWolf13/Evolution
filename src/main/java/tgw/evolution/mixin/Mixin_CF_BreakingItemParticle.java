package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.physics.Physics;

@Mixin(BreakingItemParticle.class)
public abstract class Mixin_CF_BreakingItemParticle extends TextureSheetParticle {

    @Mutable @Shadow @Final @RestoreFinal private float uo;
    @Mutable @Shadow @Final @RestoreFinal private float vo;

    @ModifyConstructor
    public Mixin_CF_BreakingItemParticle(ClientLevel level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, 0, 0, 0);
        this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, 0).getParticleIcon());
        this.gravity = (float) Physics.getRestLocalGravity(level, y, z);
        this.quadSize /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
    }
}
