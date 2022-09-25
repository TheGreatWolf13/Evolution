package tgw.evolution.network;

import net.minecraftforge.network.NetworkEvent;

public interface IPacketHandler {

    void handleMultiplayerPause(boolean paused);

    void handlePlaySoundEntityEmitted(PacketSCPlaySoundEntityEmitted packet, NetworkEvent.Context context);

    void handleSyncServerConfig(String filename, byte[] data);
}
