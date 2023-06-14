package tgw.evolution.patches;

import tgw.evolution.client.renderer.chunk.EvLevelRenderer;

public interface IMinecraftPatch {

    boolean isMultiplayerPaused();

    EvLevelRenderer lvlRenderer();

    void setMultiplayerPaused(boolean paused);
}
