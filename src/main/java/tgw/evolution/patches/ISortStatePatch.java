package tgw.evolution.patches;

import tgw.evolution.util.collection.FList;

public interface ISortStatePatch {

    FList getNewSortingPoints();

    void putNewSortingPoints(FList newSortingPoints);
}
