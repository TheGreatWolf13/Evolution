package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchSortState;
import tgw.evolution.util.collection.lists.FArrayList;
import tgw.evolution.util.collection.lists.FList;

@Mixin(BufferBuilder.SortState.class)
public abstract class MixinBufferBuilder_SortState implements PatchSortState {

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
