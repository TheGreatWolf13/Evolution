package tgw.evolution.patches;

public interface IMinecraftServerPatch {

    boolean isMultiplayerPaused();

    void setMultiplayerPaused(boolean paused);
}
