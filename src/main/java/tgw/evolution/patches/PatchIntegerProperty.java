package tgw.evolution.patches;

import tgw.evolution.util.collection.sets.ISet;

public interface PatchIntegerProperty {

    default String getName(int value) {
        throw new AbstractMethodError();
    }

    default int maxValue() {
        throw new AbstractMethodError();
    }

    default int minValue() {
        throw new AbstractMethodError();
    }

    default ISet values() {
        throw new AbstractMethodError();
    }
}
