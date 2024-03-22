package tgw.evolution.util.collection;

import tgw.evolution.Evolution;

public interface ICollectionExtension {

    boolean LIST_CHECKS = true;
    boolean CHECKS = false;

    void clear();

    default void deprecatedListMethod() {
        if (LIST_CHECKS) {
            Evolution.deprecatedMethod();
        }
    }

    default void reset() {
        this.clear();
        this.trimCollection();
    }

    void trimCollection();
}
