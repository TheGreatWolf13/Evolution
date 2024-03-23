package tgw.evolution.util.collection;

import tgw.evolution.Evolution;

public interface ICollectionExtension {

    boolean LIST_CHECKS = true;
    boolean SET_CHECKS = true;
    boolean CHECKS = false;
    long ITERATION_END = 0xFFFF_FFFFL;

    void clear();

    default void deprecatedListMethod() {
        if (LIST_CHECKS) {
            Evolution.deprecatedMethod();
        }
    }

    default void deprecatedSetMethod() {
        if (SET_CHECKS) {
            Evolution.deprecatedMethod();
        }
    }

    default void reset() {
        this.clear();
        this.trimCollection();
    }

    void trimCollection();
}
