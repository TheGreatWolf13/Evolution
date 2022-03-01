package tgw.evolution.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.IClientboundLoginPacketPatch;

@Mixin(ClientboundLoginPacket.class)
public abstract class ClientboundLoginPacketMixin implements IClientboundLoginPacketPatch {

    private long daytime;
    private Vec3 motion = Vec3.ZERO;

    @Override
    public long getDaytime() {
        return this.daytime;
    }

    @Override
    public Vec3 getMotion() {
        return this.motion;
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "TAIL"))
    private void onRead(FriendlyByteBuf buffer, CallbackInfo ci) {
        this.daytime = buffer.readLong();
        this.motion = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Inject(method = "write", at = @At(value = "TAIL"))
    private void onWrite(FriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeLong(this.daytime);
        buffer.writeDouble(this.motion.x);
        buffer.writeDouble(this.motion.y);
        buffer.writeDouble(this.motion.z);
    }

    @Override
    public void setDaytime(long daytime) {
        this.daytime = daytime;
    }

    @Override
    public void setMotion(Vec3 motion) {
        this.motion = motion;
    }
}
