package tgw.evolution.patches;

public interface PatchKeyMapping {

    default boolean consumeAllClicks() {
        throw new AbstractMethodError();
    }
}
