package tgw.evolution.mixin;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.ISJoinGamePacketPatch;

@Mixin(SJoinGamePacket.class)
public abstract class SJoinGamePacketMixin implements ISJoinGamePacketPatch {

    private long daytime;
    private Vector3d motion = Vector3d.ZERO;

    @Override
    public long getDaytime() {
        return this.daytime;
    }

    @Override
    public Vector3d getMotion() {
        return this.motion;
    }

    @Inject(method = "read", at = @At(value = "TAIL"))
    private void onRead(PacketBuffer buffer, CallbackInfo ci) {
        this.daytime = buffer.readLong();
        this.motion = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Inject(method = "write", at = @At(value = "TAIL"))
    private void onWrite(PacketBuffer buffer, CallbackInfo ci) {
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
    public void setMotion(Vector3d motion) {
        this.motion = motion;
    }
}
