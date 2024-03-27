package tgw.evolution.mixin;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.obj.FastImmutableTable;
import tgw.evolution.patches.obj.StatePropertyTableCache;
import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.collection.sets.ISet;
import tgw.evolution.util.collection.sets.RSet;

import java.util.Map;

@Mixin(StateHolder.class)
public abstract class MixinStateHolder<O, S> {

    @Shadow @Final protected O owner;
    @Shadow private @Nullable Table<Property<?>, Comparable<?>, S> neighbours;
    @Shadow @Final private ImmutableMap<Property<?>, Comparable<?>> values;

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
            if (property instanceof IntegerProperty p) {
                ISet values = p.values();
                for (long it = values.beginIteration(); values.hasNextIteration(it); it = values.nextEntry(it)) {
                    Integer value = values.getIteration(it);
                    if (value != entry.getValue()) {
                        table.put(property, value, pPossibleStateMap.get(this.makeNeighbourValues(property, value)));
                    }
                }
            }
            else if (property instanceof BooleanProperty p) {
                if (Boolean.TRUE != entry.getValue()) {
                    table.put(property, Boolean.TRUE, pPossibleStateMap.get(this.makeNeighbourValues(property, Boolean.TRUE)));
                }
                if (Boolean.FALSE != entry.getValue()) {
                    table.put(property, Boolean.FALSE, pPossibleStateMap.get(this.makeNeighbourValues(property, Boolean.FALSE)));
                }
            }
            else if (property instanceof EnumProperty p) {
                RSet<? extends Comparable<?>> values = p.values();
                for (long it = values.beginIteration(); values.hasNextIteration(it); it = values.nextEntry(it)) {
                    Comparable<?> value = values.getIteration(it);
                    if (value != entry.getValue()) {
                        table.put(property, value, pPossibleStateMap.get(this.makeNeighbourValues(property, value)));
                    }
                }
            }
            else {
                throw new UnregisteredFeatureException("Unregistered property for populateNeighbours: " + property.getClass().getCanonicalName());
            }
        }
        this.neighbours = new FastImmutableTable<>(table.isEmpty() ? table : ArrayTable.create(table), StatePropertyTableCache.getTableCache(this.owner));
    }

    @Shadow
    protected abstract Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> pProperty, Comparable<?> pValue);
}
