package tgw.evolution.patches;

public interface PatchChannel {

    default void setSelfPosition(double x, double y, double z) {
        throw new AbstractMethodError();
    }
}
