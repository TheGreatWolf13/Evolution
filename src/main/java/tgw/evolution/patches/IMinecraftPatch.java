package tgw.evolution.patches;

public interface IMinecraftPatch {

    boolean isMultiplayerPaused();

    void setMultiplayerPaused(boolean paused);
}
