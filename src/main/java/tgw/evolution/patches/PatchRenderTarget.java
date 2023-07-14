package tgw.evolution.patches;

public interface PatchRenderTarget {

    default void enableStencil() {
        throw new AbstractMethodError();
    }

    default boolean isStencilEnabled() {
        throw new AbstractMethodError();
    }
}
