package tgw.evolution.patches;

public interface PatchIdMap<T> {

    default long beginIteration() {
        throw new AbstractMethodError();
    }

    default T getIteration(long it) {
        throw new AbstractMethodError();
    }

    default boolean hasNextIteration(long it) {
        throw new AbstractMethodError();
    }

    default long nextEntry(long it) {
        throw new AbstractMethodError();
    }
}
