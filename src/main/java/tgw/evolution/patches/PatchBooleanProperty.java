package tgw.evolution.patches;

import tgw.evolution.util.collection.sets.RSet;

public interface PatchBooleanProperty {

    RSet<Boolean> VALUES = RSet.of(true, false);

    default String getName(boolean value) {
        throw new AbstractMethodError();
    }
}
