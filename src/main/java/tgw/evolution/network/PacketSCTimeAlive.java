package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class PacketSCTimeAlive implements Packet<ClientGamePacketListener> {

    public final long timeSinceLastDeath;

    public PacketSCTimeAlive(long timeSinceLastDeath) {
        this.timeSinceLastDeath = timeSinceLastDeath;
    }

    public PacketSCTimeAlive(FriendlyByteBuf buf) {
        this(buf.readVarLong());
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleTimeAlive(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarLong(this.timeSinceLastDeath);
    }
}
