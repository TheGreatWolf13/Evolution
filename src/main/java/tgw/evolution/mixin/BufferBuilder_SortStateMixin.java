package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.ISortStatePatch;
import tgw.evolution.util.collection.FArrayList;
import tgw.evolution.util.collection.FList;

@Mixin(BufferBuilder.SortState.class)
public abstract class BufferBuilder_SortStateMixin implements ISortStatePatch {

    private FList newSortingPoints;

    @Override
    public FList getNewSortingPoints() {
        return this.newSortingPoints;
    }

    @Override
    public void putNewSortingPoints(FList newSortingPoints) {
        this.newSortingPoints = new FArrayList(newSortingPoints);
    }
}
