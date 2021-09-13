package tgw.evolution.mixin;

import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.patches.IEffectInstancePatch;
import tgw.evolution.patches.ISPlayEntityEffectPacketPatch;

@Mixin(SPlayEntityEffectPacket.class)
public abstract class SPlayEntityEffectPacketMixin implements ISPlayEntityEffectPacketPatch {

    @Shadow
    private byte flags;

    @Override
    public boolean isInfinite() {
        return (this.flags & 8) != 0;
    }

    @Inject(method = "<init>(ILnet/minecraft/potion/EffectInstance;)V", at = @At(value = "TAIL"))
    private void onConstructor(int entityId, EffectInstance effect, CallbackInfo ci) {
        if (((IEffectInstancePatch) effect).isInfinite()) {
            this.flags |= 8;
        }
    }

    @Inject(method = "isSuperLongDuration", at = @At(value = "HEAD"), cancellable = true)
    private void onIsMaxDuration(CallbackInfoReturnable<Boolean> cir) {
        if (this.isInfinite()) {
            cir.setReturnValue(true);
        }
    }
}
