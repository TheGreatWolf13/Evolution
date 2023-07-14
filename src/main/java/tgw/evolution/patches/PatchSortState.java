package tgw.evolution.patches;

import tgw.evolution.util.collection.lists.FList;

public interface PatchSortState {

    FList getNewSortingPoints();

    void putNewSortingPoints(FList newSortingPoints);
}
