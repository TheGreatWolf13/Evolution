package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.patches.ILevelPatch;

import java.util.function.Supplier;

public class PacketSCBlockDestruction implements IPacket {

    private final int id;
    private final long pos;
    private final int progress;

    public PacketSCBlockDestruction(int id, long pos, int progress) {
        this.id = id;
        this.pos = pos;
        this.progress = progress;
    }

    public static PacketSCBlockDestruction decode(FriendlyByteBuf buffer) {
        return new PacketSCBlockDestruction(buffer.readVarInt(), buffer.readLong(), buffer.readVarInt());
    }

    public static void encode(PacketSCBlockDestruction packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.id);
        buffer.writeLong(packet.pos);
        buffer.writeVarInt(packet.progress);
    }

    public static void handle(PacketSCBlockDestruction packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> ((ILevelPatch) Evolution.PROXY.getClientLevel()).destroyBlockProgress(packet.id, packet.pos, packet.progress));
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
