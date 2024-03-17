package tgw.evolution.patches;

public interface PatchDebugRenderer {

    default void setRenderHeightmap(boolean render) {
        throw new AbstractMethodError();
    }
}
