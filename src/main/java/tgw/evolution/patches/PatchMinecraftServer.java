package tgw.evolution.patches;

public interface PatchMinecraftServer {

    default boolean isMultiplayerPaused() {
        throw new AbstractMethodError();
    }

    default void setMultiplayerPaused(boolean paused) {
        throw new AbstractMethodError();
    }
}
