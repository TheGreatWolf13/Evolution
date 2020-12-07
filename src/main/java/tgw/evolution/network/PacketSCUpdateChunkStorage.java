package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.capabilities.chunkstorage.ChunkStorage;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.capabilities.chunkstorage.IChunkStorage;

import java.util.Optional;
import java.util.function.Supplier;

public class PacketSCUpdateChunkStorage implements IPacket {

    private final ChunkPos chunkPos;
    private final int nitrogen;
    private final int phosphorus;
    private final int potassium;
    private final int water;
    private final int carbonDioxide;
    private final int oxygen;
    private final int gasNitrogen;

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

    public static PacketSCUpdateChunkStorage decode(PacketBuffer buffer) {
        return new PacketSCUpdateChunkStorage(new ChunkPos(buffer.readInt(), buffer.readInt()),
                                              buffer.readInt(),
                                              buffer.readInt(),
                                              buffer.readInt(),
                                              buffer.readInt(),
                                              buffer.readInt(),
                                              buffer.readInt(),
                                              buffer.readInt());
    }

    public static void encode(PacketSCUpdateChunkStorage message, PacketBuffer buffer) {
        buffer.writeInt(message.chunkPos.x);
        buffer.writeInt(message.chunkPos.z);
        buffer.writeInt(message.nitrogen);
        buffer.writeInt(message.phosphorus);
        buffer.writeInt(message.potassium);
        buffer.writeInt(message.water);
        buffer.writeInt(message.carbonDioxide);
        buffer.writeInt(message.oxygen);
        buffer.writeInt(message.gasNitrogen);
    }

    public static void handle(PacketSCUpdateChunkStorage packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                Optional<World> optionalWorld = LogicalSidedProvider.CLIENTWORLD.get(context.get().getDirection().getReceptionSide());
                optionalWorld.ifPresent(world -> ChunkStorageCapability.getChunkStorage(world, packet.chunkPos).ifPresent(chunkStorage -> {
                    if (!(chunkStorage instanceof ChunkStorage)) {
                        return;
                    }
                    ((ChunkStorage) chunkStorage).setElement(EnumStorage.NITROGEN, packet.nitrogen);
                    ((ChunkStorage) chunkStorage).setElement(EnumStorage.PHOSPHORUS, packet.phosphorus);
                    ((ChunkStorage) chunkStorage).setElement(EnumStorage.POTASSIUM, packet.potassium);
                    ((ChunkStorage) chunkStorage).setElement(EnumStorage.WATER, packet.water);
                    ((ChunkStorage) chunkStorage).setElement(EnumStorage.CARBON_DIOXIDE, packet.carbonDioxide);
                    ((ChunkStorage) chunkStorage).setElement(EnumStorage.OXYGEN, packet.oxygen);
                    ((ChunkStorage) chunkStorage).setElement(EnumStorage.GAS_NITROGEN, packet.gasNitrogen);
                }));
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
