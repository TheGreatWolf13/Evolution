package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.player.Player;

public class PacketSCUpdateCameraTilt implements Packet<ClientGamePacketListener> {

    public final float attackedAtYaw;

    public PacketSCUpdateCameraTilt(FriendlyByteBuf buf) {
        this.attackedAtYaw = buf.readFloat();
    }

    public PacketSCUpdateCameraTilt(Player player) {
        this.attackedAtYaw = player.hurtDir;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleUpdateCameraTilt(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(this.attackedAtYaw);
    }
}
