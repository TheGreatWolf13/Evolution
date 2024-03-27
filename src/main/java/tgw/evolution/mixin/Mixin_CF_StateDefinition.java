package tgw.evolution.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchStateDefinition;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(StateDefinition.class)
public abstract class Mixin_CF_StateDefinition<O, S extends StateHolder<O, S>> implements PatchStateDefinition<S> {

    @Mutable @Shadow @Final @RestoreFinal private O owner;
    @Mutable @Shadow @Final @RestoreFinal private ImmutableSortedMap<String, Property<?>> propertiesByName;
    @Shadow @Final @DeleteField private ImmutableList<S> states;
    @Unique private final OList<S> states_;

    @ModifyConstructor
    public Mixin_CF_StateDefinition(Function<O, S> function, O object, StateDefinition.Factory<O, S> factory, Map<String, Property<?>> map) {
        this.owner = object;
        this.propertiesByName = ImmutableSortedMap.copyOf(map);
        Supplier<S> supplier = func(function, object);
        MapCodec<S> mapCodec = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));
        for (Map.Entry<String, Property<?>> entry : this.propertiesByName.entrySet()) {
            mapCodec = appendPropertyCodec(mapCodec, supplier, entry.getKey(), entry.getValue());
        }
        int numStates = 1;
        //noinspection RedundantOperationOnEmptyContainer
        for (Property<?> property : this.propertiesByName.values()) {
            numStates *= property.getPossibleValues().size();
        }
        OList<S> listOfStates = new OArrayList<>(numStates);
        OList<ImmutableMap.Builder<Property<?>, Comparable<?>>> listOfMapsOfPropertiesOfEveryState = new OArrayList<>(numStates);
        for (int i = 0; i < numStates; ++i) {
            //noinspection ObjectAllocationInLoop
            listOfMapsOfPropertiesOfEveryState.add(new ImmutableMap.Builder<>());
        }
        int computedPossibilities = 1;
        for (Property<?> property : this.propertiesByName.values()) {
            Collection<Comparable<?>> possibleValues = (Collection<Comparable<?>>) property.getPossibleValues();
            int possibilities = possibleValues.size();
            int repeat = numStates / possibilities;
            int groupSize = repeat / computedPossibilities;
            int groupNum = repeat / groupSize;
            int spacing = (possibilities - 1) * groupSize;
            int computedValues = 0;
            for (Comparable<?> value : possibleValues) {
                int index = groupSize * computedValues;
                for (int i = 0; i < groupNum; ++i) {
                    for (int j = 0; j < groupSize; ++j) {
                        listOfMapsOfPropertiesOfEveryState.get(index++).put(property, value);
                    }
                    index += spacing;
                }
                ++computedValues;
            }
            computedPossibilities *= possibilities;
        }
        Map<Map<Property<?>, Comparable<?>>, S> mapToPopulate = Maps.newLinkedHashMap();
        for (int i = 0, len = listOfMapsOfPropertiesOfEveryState.size(); i < len; ++i) {
            ImmutableMap<Property<?>, Comparable<?>> immutableMap = listOfMapsOfPropertiesOfEveryState.get(i).build();
            S stateHolder = factory.create(object, immutableMap, mapCodec);
            mapToPopulate.put(immutableMap, stateHolder);
            listOfStates.add(stateHolder);
        }
        for (int i = 0, len = listOfStates.size(); i < len; ++i) {
            listOfStates.get(i).populateNeighbours(mapToPopulate);
        }
        listOfStates.trim();
        this.states_ = listOfStates.view();
    }

    @Shadow
    private static <S extends StateHolder<?, S>, T extends Comparable<T>> MapCodec<S> appendPropertyCodec(MapCodec<S> mapCodec, Supplier<S> supplier, String string, Property<T> property) {
        throw new AbstractMethodError();
    }

    @Unique
    private static <S, O> Supplier<S> func(Function<O, S> func, O o) {
        return () -> func.apply(o);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public S any() {
        return this.states_.get(0);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public ImmutableList<S> getPossibleStates() {
        Evolution.deprecatedMethod();
        return ImmutableList.copyOf(this.states_);
    }

    @Override
    public OList<S> getPossibleStates_() {
        return this.states_;
    }
}
