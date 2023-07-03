package tgw.evolution.patches;

import tgw.evolution.client.renderer.chunk.EvLevelRenderer;

public interface IMinecraftPatch {

    boolean isMultiplayerPaused();

    EvLevelRenderer lvlRenderer();

    void resetUseHeld();

    void setMultiplayerPaused(boolean paused);
}
