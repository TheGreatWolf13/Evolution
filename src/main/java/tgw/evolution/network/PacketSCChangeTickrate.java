package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.patches.PatchClientPacketListener;

public class PacketSCChangeTickrate implements Packet<ClientGamePacketListener> {

    public final float tickrate;

    public PacketSCChangeTickrate(float tickrate) {
        this.tickrate = tickrate;
    }

    public PacketSCChangeTickrate(FriendlyByteBuf buf) {
        this.tickrate = buf.readFloat();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        ((PatchClientPacketListener) listener).handleChangeTickrate(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(this.tickrate);
    }
}
