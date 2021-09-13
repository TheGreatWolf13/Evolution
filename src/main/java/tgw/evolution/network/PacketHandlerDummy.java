package tgw.evolution.network;

import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketHandlerDummy implements IPacketHandler {

    @Override
    public void handlePlaySoundEntityEmitted(PacketSCPlaySoundEntityEmitted packet, Supplier<NetworkEvent.Context> context) {
        throw new AssertionError("Should only run on the Client!");
    }
}
