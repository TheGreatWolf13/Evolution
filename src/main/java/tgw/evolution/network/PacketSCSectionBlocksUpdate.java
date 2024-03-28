package tgw.evolution.network;

import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import tgw.evolution.util.collection.sets.SSet;

public class PacketSCSectionBlocksUpdate implements Packet<ClientGamePacketListener> {

    public final short[] positions;
    public final long secPos;
    public final BlockState[] states;
    public final boolean suppressLightUpdates;

    public PacketSCSectionBlocksUpdate(long secPos, SSet set, LevelChunkSection section, boolean suppressLightUpdates) {
        this.secPos = secPos;
        this.suppressLightUpdates = suppressLightUpdates;
        int size = set.size();
        this.positions = new short[size];
        this.states = new BlockState[size];
        int j = 0;
        for (long it = set.beginIteration(); set.hasNextIteration(it); it = set.nextEntry(it)) {
            short relative = set.getIteration(it);
            this.positions[j] = relative;
            this.states[j] = section.getBlockState(SectionPos.sectionRelativeX(relative), SectionPos.sectionRelativeY(relative), SectionPos.sectionRelativeZ(relative));
        }
    }

    public PacketSCSectionBlocksUpdate(FriendlyByteBuf buf) {
        this.secPos = buf.readLong();
        this.suppressLightUpdates = buf.readBoolean();
        int size = buf.readVarInt();
        this.positions = new short[size];
        this.states = new BlockState[size];
        for (int i = 0; i < size; ++i) {
            long l = buf.readVarLong();
            this.positions[i] = (short) (l & 0b1111_1111_1111L);
            this.states[i] = Block.stateById((int) (l >>> 12));
        }
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleSectionBlocksUpdate(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(this.secPos);
        buf.writeBoolean(this.suppressLightUpdates);
        buf.writeVarInt(this.positions.length);
        for (int i = 0, len = this.positions.length; i < len; i++) {
            buf.writeVarLong((long) Block.getId(this.states[i]) << 12 | this.positions[i]);
        }
    }
}
