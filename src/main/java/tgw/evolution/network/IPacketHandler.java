package tgw.evolution.network;

public interface IPacketHandler {

    void handleMultiplayerPause(boolean paused);

    void handleSyncServerConfig(String filename, byte[] data);
}
