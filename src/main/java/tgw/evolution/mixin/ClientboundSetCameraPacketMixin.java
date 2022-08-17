package tgw.evolution.mixin;

import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IClientboundSetCameraPacketPatch;

@Mixin(ClientboundSetCameraPacket.class)
public abstract class ClientboundSetCameraPacketMixin implements IClientboundSetCameraPacketPatch {

    @Shadow
    @Final
    private int cameraId;

    @Override
    public int getId() {
        return this.cameraId;
    }
}
