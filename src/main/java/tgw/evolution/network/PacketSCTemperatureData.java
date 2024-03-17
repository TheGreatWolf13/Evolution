package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.capabilities.player.CapabilityTemperature;
import tgw.evolution.patches.PatchClientGamePacketListener;

public class PacketSCTemperatureData implements Packet<ClientGamePacketListener> {

    public final int currentMaxComfort;
    public final int currentMinComfort;
    public final int currentTemp;

    public PacketSCTemperatureData(FriendlyByteBuf buf) {
        this.currentTemp = buf.readVarInt();
        this.currentMinComfort = buf.readVarInt();
        this.currentMaxComfort = buf.readVarInt();
    }

    public PacketSCTemperatureData(CapabilityTemperature temperature) {
        this.currentTemp = temperature.getCurrentTemperature();
        this.currentMaxComfort = temperature.getCurrentMaxComfort();
        this.currentMinComfort = temperature.getCurrentMinComfort();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleTemperatureData(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.currentTemp);
        buf.writeVarInt(this.currentMinComfort);
        buf.writeVarInt(this.currentMaxComfort);
    }
}
