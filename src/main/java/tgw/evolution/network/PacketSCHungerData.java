package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.capabilities.food.HungerStats;
import tgw.evolution.capabilities.food.IHunger;

import java.util.function.Supplier;

public class PacketSCHungerData implements IPacket {

    private final short hungerLevel;
    private final short saturationLevel;

    public PacketSCHungerData(IHunger hunger) {
        this.hungerLevel = (short) hunger.getHungerLevel();
        this.saturationLevel = (short) hunger.getSaturationLevel();
    }

    private PacketSCHungerData(short hungerLevel, short saturationLevel) {
        this.hungerLevel = hungerLevel;
        this.saturationLevel = saturationLevel;
    }

    public static PacketSCHungerData decode(FriendlyByteBuf buffer) {
        return new PacketSCHungerData(buffer.readShort(), buffer.readShort());
    }

    public static void encode(PacketSCHungerData packet, FriendlyByteBuf buffer) {
        buffer.writeShort(packet.hungerLevel);
        buffer.writeShort(packet.saturationLevel);
    }

    public static void handle(PacketSCHungerData packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                IHunger hunger = HungerStats.CLIENT_INSTANCE;
                hunger.setHungerLevel(packet.hungerLevel);
                hunger.setSaturationLevel(packet.saturationLevel);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
