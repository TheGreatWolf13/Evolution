package tgw.evolution.patches;

import it.unimi.dsi.fastutil.floats.FloatList;

public interface ISortStatePatch {

    FloatList getNewSortingPoints();

    void putNewSortingPoints(FloatList newSortingPoints);
}
