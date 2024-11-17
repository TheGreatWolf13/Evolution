package tgw.evolution.patches;

public interface PatchPathNavigation {

    default boolean isStableDestination(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean shouldRecomputePath_(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
