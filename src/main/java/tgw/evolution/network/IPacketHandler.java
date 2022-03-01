package tgw.evolution.network;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface IPacketHandler {

    void handleMultiplayerPause(boolean paused);

    void handlePlaySoundEntityEmitted(PacketSCPlaySoundEntityEmitted packet, Supplier<NetworkEvent.Context> context);

    void handleSyncServerConfig(String filename, byte[] data);
}
