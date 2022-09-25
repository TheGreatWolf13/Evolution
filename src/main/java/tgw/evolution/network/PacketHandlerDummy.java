package tgw.evolution.network;

import net.minecraftforge.network.NetworkEvent;

public class PacketHandlerDummy implements IPacketHandler {

    @Override
    public void handleMultiplayerPause(boolean paused) {
        throw new AssertionError("Should only run on the Client!");
    }

    @Override
    public void handlePlaySoundEntityEmitted(PacketSCPlaySoundEntityEmitted packet, NetworkEvent.Context context) {
        throw new AssertionError("Should only run on the Client!");
    }

    @Override
    public void handleSyncServerConfig(String filename, byte[] data) {
        throw new AssertionError("Should only run on the Client!");
    }
}
