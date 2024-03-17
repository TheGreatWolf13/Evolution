package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PacketSCBlockBreakAck implements Packet<ClientGamePacketListener> {

    public final ServerboundPlayerActionPacket.Action action;
    public final boolean allGood;
    public final long pos;
    public final BlockState state;

    public PacketSCBlockBreakAck(long pos, BlockState state, ServerboundPlayerActionPacket.Action action, boolean allGood) {
        this.pos = pos;
        this.state = state;
        this.action = action;
        this.allGood = allGood;
    }

    public PacketSCBlockBreakAck(FriendlyByteBuf buf) {
        this.pos = buf.readLong();
        this.state = Block.stateById(buf.readVarInt());
        this.action = buf.readEnum(ServerboundPlayerActionPacket.Action.class);
        this.allGood = buf.readBoolean();
    }

    public static PacketSCBlockBreakAck decode(FriendlyByteBuf buf) {
        return new PacketSCBlockBreakAck(buf);
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleBlockBreakAck(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(this.pos);
        buf.writeVarInt(Block.getId(this.state));
        buf.writeEnum(this.action);
        buf.writeBoolean(this.allGood);
    }
}
