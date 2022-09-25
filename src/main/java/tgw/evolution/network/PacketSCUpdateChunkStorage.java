package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.ChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.capabilities.chunkstorage.IChunkStorage;
import tgw.evolution.init.EvolutionCapabilities;

import java.util.function.Supplier;

public class PacketSCUpdateChunkStorage implements IPacket {

    private final int carbonDioxide;
    private final ChunkPos chunkPos;
    private final int gasNitrogen;
    private final int nitrogen;
    private final int oxygen;
    private final int phosphorus;
    private final int potassium;
    private final int water;

    public PacketSCUpdateChunkStorage(IChunkStorage chunkStorage) {
        this.chunkPos = chunkStorage.getChunkPos();
        this.nitrogen = chunkStorage.getElementStored(EnumStorage.NITROGEN);
        this.phosphorus = chunkStorage.getElementStored(EnumStorage.PHOSPHORUS);
        this.potassium = chunkStorage.getElementStored(EnumStorage.POTASSIUM);
        this.water = chunkStorage.getElementStored(EnumStorage.WATER);
        this.carbonDioxide = chunkStorage.getElementStored(EnumStorage.CARBON_DIOXIDE);
        this.oxygen = chunkStorage.getElementStored(EnumStorage.OXYGEN);
        this.gasNitrogen = chunkStorage.getElementStored(EnumStorage.GAS_NITROGEN);
    }

    private PacketSCUpdateChunkStorage(ChunkPos chunkPos,
                                       int nitrogen,
                                       int phosphorus,
                                       int potassium,
                                       int water,
                                       int carbonDioxide,
                                       int oxygen,
                                       int gasNitrogen) {
        this.chunkPos = chunkPos;
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.water = water;
        this.carbonDioxide = carbonDioxide;
        this.oxygen = oxygen;
        this.gasNitrogen = gasNitrogen;
    }

    public static PacketSCUpdateChunkStorage decode(FriendlyByteBuf buffer) {
        return new PacketSCUpdateChunkStorage(new ChunkPos(buffer.readVarInt(), buffer.readVarInt()),
                                              buffer.readVarInt(),
                                              buffer.readVarInt(),
                                              buffer.readVarInt(),
                                              buffer.readVarInt(),
                                              buffer.readVarInt(),
                                              buffer.readVarInt(),
                                              buffer.readVarInt());
    }

    public static void encode(PacketSCUpdateChunkStorage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.chunkPos.x);
        buffer.writeVarInt(message.chunkPos.z);
        buffer.writeVarInt(message.nitrogen);
        buffer.writeVarInt(message.phosphorus);
        buffer.writeVarInt(message.potassium);
        buffer.writeVarInt(message.water);
        buffer.writeVarInt(message.carbonDioxide);
        buffer.writeVarInt(message.oxygen);
        buffer.writeVarInt(message.gasNitrogen);
    }

    public static void handle(PacketSCUpdateChunkStorage packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                Level level = Evolution.PROXY.getClientLevel();
                IChunkStorage storage = EvolutionCapabilities.getCapabilityOrThrow(level.getChunk(packet.chunkPos.x, packet.chunkPos.z),
                                                                                   CapabilityChunkStorage.INSTANCE);
                if (storage instanceof ChunkStorage cs) {
                    cs.setElement(EnumStorage.NITROGEN, packet.nitrogen);
                    cs.setElement(EnumStorage.PHOSPHORUS, packet.phosphorus);
                    cs.setElement(EnumStorage.POTASSIUM, packet.potassium);
                    cs.setElement(EnumStorage.WATER, packet.water);
                    cs.setElement(EnumStorage.CARBON_DIOXIDE, packet.carbonDioxide);
                    cs.setElement(EnumStorage.OXYGEN, packet.oxygen);
                    cs.setElement(EnumStorage.GAS_NITROGEN, packet.gasNitrogen);
                }
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
