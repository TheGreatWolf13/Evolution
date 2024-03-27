package tgw.evolution.patches;

import tgw.evolution.util.collection.lists.OList;

public interface PatchStateDefinition<S> {

    default OList<S> getPossibleStates_() {
        throw new AbstractMethodError();
    }
}
