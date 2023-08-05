package tgw.evolution.patches;

public interface PatchPathNavigation {

    default boolean shouldRecomputePath_(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
