package tgw.evolution.patches;

public interface PatchHitResult {

    default void set(double x, double y, double z) {
        throw new AbstractMethodError();
    }

    default double x() {
        throw new AbstractMethodError();
    }

    default double y() {
        throw new AbstractMethodError();
    }

    default double z() {
        throw new AbstractMethodError();
    }
}
