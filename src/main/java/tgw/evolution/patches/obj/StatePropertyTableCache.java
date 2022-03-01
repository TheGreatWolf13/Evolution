package tgw.evolution.patches.obj;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public final class StatePropertyTableCache {

    public static final FastImmutableTableCache<Property<?>, Comparable<?>, BlockState> BLOCK_STATE_TABLE = new FastImmutableTableCache<>();
    public static final FastImmutableTableCache<Property<?>, Comparable<?>, FluidState> FLUID_STATE_TABLE = new FastImmutableTableCache<>();

    private StatePropertyTableCache() {
    }

    public static <S, O> FastImmutableTableCache<Property<?>, Comparable<?>, S> getTableCache(O owner) {
        if (owner instanceof Block) {
            return (FastImmutableTableCache<Property<?>, Comparable<?>, S>) BLOCK_STATE_TABLE;
        }
        if (owner instanceof Fluid) {
            return (FastImmutableTableCache<Property<?>, Comparable<?>, S>) FLUID_STATE_TABLE;
        }
        throw new IllegalArgumentException("");
    }
}
