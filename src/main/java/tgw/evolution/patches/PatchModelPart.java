package tgw.evolution.patches;

public interface PatchModelPart {

    default void shouldRenderChildrenEvenWhenNotVisible(boolean render) {
        throw new AbstractMethodError();
    }
}
