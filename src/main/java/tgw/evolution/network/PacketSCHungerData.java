package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.capabilities.player.CapabilityHunger;
import tgw.evolution.patches.PatchClientPacketListener;

public class PacketSCHungerData implements Packet<ClientGamePacketListener> {

    public final int hungerLevel;
    public final int saturationLevel;

    public PacketSCHungerData(FriendlyByteBuf buf) {
        this.hungerLevel = buf.readVarInt();
        this.saturationLevel = buf.readVarInt();
    }

    public PacketSCHungerData(CapabilityHunger hunger) {
        this.hungerLevel = hunger.getHungerLevel();
        this.saturationLevel = hunger.getSaturationLevel();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        ((PatchClientPacketListener) listener).handleHungerData(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.hungerLevel);
        buf.writeVarInt(this.saturationLevel);
    }
}
