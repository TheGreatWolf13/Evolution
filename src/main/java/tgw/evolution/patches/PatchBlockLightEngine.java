package tgw.evolution.patches;

public interface PatchBlockLightEngine {

    default void setColor(boolean red, boolean green, boolean blue) {
        throw new AbstractMethodError();
    }
}
