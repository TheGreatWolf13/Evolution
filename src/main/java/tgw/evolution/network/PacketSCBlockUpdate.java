package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.patches.PatchClientPacketListener;

public class PacketSCBlockUpdate implements Packet<ClientGamePacketListener> {

    public final long pos;
    public final BlockState state;

    public PacketSCBlockUpdate(long pos, BlockState state) {
        this.state = state;
        this.pos = pos;
    }

    public PacketSCBlockUpdate(BlockGetter level, int x, int y, int z) {
        this(BlockPos.asLong(x, y, z), level.getBlockState_(x, y, z));
    }

    public PacketSCBlockUpdate(FriendlyByteBuf buf) {
        this.pos = buf.readLong();
        this.state = Block.stateById(buf.readVarInt());
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        ((PatchClientPacketListener) listener).handleBlockUpdate(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(this.pos);
        buf.writeVarInt(Block.getId(this.state));
    }
}
