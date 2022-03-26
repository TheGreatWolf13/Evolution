package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.ISortStatePatch;

@Mixin(BufferBuilder.SortState.class)
public abstract class BufferBuilder_SortStateMixin implements ISortStatePatch {

    private FloatList newSortingPoints;

    @Override
    public FloatList getNewSortingPoints() {
        return this.newSortingPoints;
    }

    @Override
    public void putNewSortingPoints(FloatList newSortingPoints) {
        this.newSortingPoints = new FloatArrayList(newSortingPoints);
    }
}
