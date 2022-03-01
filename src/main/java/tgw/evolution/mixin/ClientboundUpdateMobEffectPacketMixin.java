package tgw.evolution.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.patches.IClientboundUpdateMobEffectPacketPatch;
import tgw.evolution.patches.IEffectInstancePatch;

@Mixin(ClientboundUpdateMobEffectPacket.class)
public abstract class ClientboundUpdateMobEffectPacketMixin implements IClientboundUpdateMobEffectPacketPatch {

    private boolean infinite;

    @Override
    public boolean isInfinite() {
        return this.infinite;
    }

    @Inject(method = "<init>(ILnet/minecraft/world/effect/MobEffectInstance;)V", at = @At(value = "TAIL"))
    private void onConstructor(int entityId, MobEffectInstance effect, CallbackInfo ci) {
        this.infinite = ((IEffectInstancePatch) effect).isInfinite();
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "TAIL"))
    private void onConstructor(FriendlyByteBuf buffer, CallbackInfo ci) {
        this.infinite = buffer.readBoolean();
    }

    @Inject(method = "isSuperLongDuration", at = @At(value = "HEAD"), cancellable = true)
    private void onIsMaxDuration(CallbackInfoReturnable<Boolean> cir) {
        if (this.infinite) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "write", at = @At(value = "TAIL"))
    private void onWrite(FriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(this.infinite);
    }
}
