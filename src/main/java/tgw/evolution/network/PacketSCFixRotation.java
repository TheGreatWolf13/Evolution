package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import tgw.evolution.patches.PatchClientPacketListener;

public class PacketSCFixRotation implements Packet<ClientGamePacketListener> {

    public final int entityId;
    public final byte xRot;
    public final byte yHeadRot;
    public final byte yRot;

    public PacketSCFixRotation(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.xRot = buf.readByte();
        this.yRot = buf.readByte();
        this.yHeadRot = buf.readByte();
    }

    public PacketSCFixRotation(Entity entity) {
        this.entityId = entity.getId();
        this.xRot = (byte) Mth.floor(entity.getXRot() * 256.0f / 360.0f);
        this.yRot = (byte) Mth.floor(entity.getYRot() * 256.0f / 360.0f);
        this.yHeadRot = (byte) Mth.floor(entity.getYHeadRot() * 256.0f / 360.0f);
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        ((PatchClientPacketListener) listener).handleFixRotation(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeByte(this.xRot);
        buf.writeByte(this.yRot);
        buf.writeByte(this.yHeadRot);
    }
}
