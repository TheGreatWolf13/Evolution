package tgw.evolution.patches;

public interface PatchWorldGenLevel {

    default boolean ensureCanWrite_(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
