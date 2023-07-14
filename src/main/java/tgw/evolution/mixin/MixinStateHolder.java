package tgw.evolution.mixin;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.obj.FastImmutableTable;
import tgw.evolution.patches.obj.StatePropertyTableCache;

import java.util.Map;

@Mixin(StateHolder.class)
public abstract class MixinStateHolder<O, S> {

    @Shadow @Final protected O owner;
    @Shadow private @Nullable Table<Property<?>, Comparable<?>, S> neighbours;
    @Shadow @Final private ImmutableMap<Property<?>, Comparable<?>> values;

    @Shadow
    protected abstract Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> pProperty, Comparable<?> pValue);

    /**
     * @author TheGreatWolf
     * @reason Add fast table
     */
    @Overwrite
    public void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> pPossibleStateMap) {
        //noinspection VariableNotUsedInsideIf
        if (this.neighbours != null) {
            throw new IllegalStateException();
        }
        Table<Property<?>, Comparable<?>, S> table = HashBasedTable.create();
        for (Map.Entry<Property<?>, Comparable<?>> entry : this.values.entrySet()) {
            Property<?> property = entry.getKey();
            for (Comparable<?> comparable : property.getPossibleValues()) {
                if (comparable != entry.getValue()) {
                    table.put(property, comparable, pPossibleStateMap.get(this.makeNeighbourValues(property, comparable)));
                }
            }
        }
        this.neighbours = new FastImmutableTable<>(table.isEmpty() ? table : ArrayTable.create(table),
                                                   StatePropertyTableCache.getTableCache(this.owner));
    }
}
