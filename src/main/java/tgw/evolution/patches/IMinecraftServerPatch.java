package tgw.evolution.patches;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

import java.util.UUID;

public interface IMinecraftServerPatch {

    Object2BooleanMap<UUID> getMultiplayerPauseSentPackets();

    boolean isMultiplayerPaused();

    void setMultiplayerPaused(boolean paused);
}
