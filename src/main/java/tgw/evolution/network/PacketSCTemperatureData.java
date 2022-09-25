package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.capabilities.temperature.ITemperature;
import tgw.evolution.capabilities.temperature.TemperatureClient;

import java.util.function.Supplier;

public class PacketSCTemperatureData implements IPacket {

    private final int currentMaxComfort;
    private final int currentMinComfort;
    private final int currentTemp;

    private PacketSCTemperatureData(int currentTemp, int maxComfort, int minComfort) {
        this.currentTemp = currentTemp;
        this.currentMaxComfort = maxComfort;
        this.currentMinComfort = minComfort;
    }

    public PacketSCTemperatureData(ITemperature temperature) {
        this.currentTemp = temperature.getCurrentTemperature();
        this.currentMaxComfort = temperature.getCurrentMaxComfort();
        this.currentMinComfort = temperature.getCurrentMinComfort();
    }

    public static PacketSCTemperatureData decode(FriendlyByteBuf buffer) {
        return new PacketSCTemperatureData(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void encode(PacketSCTemperatureData packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.currentTemp);
        buffer.writeVarInt(packet.currentMaxComfort);
        buffer.writeVarInt(packet.currentMinComfort);
    }

    public static void handle(PacketSCTemperatureData packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                TemperatureClient temperature = TemperatureClient.CLIENT_INSTANCE;
                temperature.setCurrentTemperature(packet.currentTemp);
                temperature.setCurrentMaxComfort(packet.currentMaxComfort);
                temperature.setCurrentMinComfort(packet.currentMinComfort);
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
