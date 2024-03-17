package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.capabilities.player.CapabilityThirst;
import tgw.evolution.patches.PatchClientGamePacketListener;

public class PacketSCThirstData implements Packet<ClientGamePacketListener> {

    public final int hydrationLevel;
    public final int thirstLevel;

    public PacketSCThirstData(CapabilityThirst thirst) {
        this.thirstLevel = thirst.getThirstLevel();
        this.hydrationLevel = thirst.getHydrationLevel();
    }

    public PacketSCThirstData(FriendlyByteBuf buf) {
        this.thirstLevel = buf.readVarInt();
        this.hydrationLevel = buf.readVarInt();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleThirstData(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.thirstLevel);
        buf.writeVarInt(this.hydrationLevel);
    }
}
