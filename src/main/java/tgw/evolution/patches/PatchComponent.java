package tgw.evolution.patches;

public interface PatchComponent {

    default void resetCache() {
        throw new AbstractMethodError();
    }
}
