package tgw.evolution.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.IClientboundUpdateMobEffectPacketPatch;
import tgw.evolution.patches.IMobEffectInstancePatch;

@Mixin(ClientboundUpdateMobEffectPacket.class)
public abstract class ClientboundUpdateMobEffectPacketMixin implements IClientboundUpdateMobEffectPacketPatch {

    @Shadow
    @Final
    private byte effectAmplifier;
    @Shadow
    @Final
    private int effectDurationTicks;
    @Shadow
    @Final
    private int effectId;
    @Shadow
    @Final
    private int entityId;
    @Shadow
    @Final
    private byte flags;
    @Unique
    private boolean infinite;

    @Override
    public boolean isInfinite() {
        return this.infinite;
    }

    /**
     * @author TheGreatWolf
     * @reason Handle infinite effects
     */
    @Overwrite
    public boolean isSuperLongDuration() {
        if (this.infinite) {
            return true;
        }
        return this.effectDurationTicks == Short.MAX_VALUE;
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "TAIL"))
    private void onConstructor(FriendlyByteBuf buffer, CallbackInfo ci) {
        this.infinite = buffer.readBoolean();
    }

    @Inject(method = "<init>(ILnet/minecraft/world/effect/MobEffectInstance;)V", at = @At(value = "TAIL"))
    private void onConstructor(int entityId, MobEffectInstance effect, CallbackInfo ci) {
        this.infinite = ((IMobEffectInstancePatch) effect).isInfinite();
    }

    /**
     * @author TheGreatWolf
     * @reason Save infinite effect
     */
    @Overwrite
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeVarInt(this.effectId);
        buffer.writeByte(this.effectAmplifier);
        buffer.writeVarInt(this.effectDurationTicks);
        buffer.writeByte(this.flags);
        buffer.writeBoolean(this.infinite);
    }
}
