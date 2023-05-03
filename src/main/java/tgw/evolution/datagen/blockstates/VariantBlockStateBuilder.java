package tgw.evolution.datagen.blockstates;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.IGeneratedBlockstate;
import tgw.evolution.datagen.BlockStateProvider;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class VariantBlockStateBuilder implements IGeneratedBlockstate {
    private final Set<BlockState> coveredStates = new HashSet<>();
    private final Map<VariantBlockStateBuilder.PartialBlockstate, BlockStateProvider.ConfiguredModelList> models = new LinkedHashMap<>();
    private final Block owner;

    public VariantBlockStateBuilder(Block owner) {
        this.owner = owner;
    }

    /**
     * Assign some models to a given {@link VariantBlockStateBuilder.PartialBlockstate partial state}.
     *
     * @param state  The {@link VariantBlockStateBuilder.PartialBlockstate partial state} for which to add the models
     * @param models A set of models to add to this state
     * @return this builder
     * @throws NullPointerException     if {@code state} is {@code null}
     * @throws IllegalArgumentException if {@code models} is empty
     * @throws IllegalArgumentException if {@code state}'s owning block differs from
     *                                  the builder's
     * @throws IllegalArgumentException if {@code state} partially matches another
     *                                  state which has already been configured
     */
    public VariantBlockStateBuilder addModels(VariantBlockStateBuilder.PartialBlockstate state, ConfiguredModel... models) {
        Preconditions.checkNotNull(state, "state must not be null");
        Preconditions.checkArgument(models.length > 0, "Cannot set models to empty array");
        Preconditions.checkArgument(state.getOwner() == this.owner, "Cannot set models for a different block. Found: %s, Current: %s",
                                    state.getOwner(),
                                    this.owner);
        if (!this.models.containsKey(state)) {
            Preconditions.checkArgument(this.disjointToAll(state),
                                        "Cannot set models for a state for which a partial match has already been configured");
            this.models.put(state, new BlockStateProvider.ConfiguredModelList(models));
            for (BlockState fullState : this.owner.getStateDefinition().getPossibleStates()) {
                if (state.test(fullState)) {
                    this.coveredStates.add(fullState);
                }
            }
        }
        else {
            this.models.computeIfPresent(state, ($, cml) -> cml.append(models));
        }
        return this;
    }

    private boolean disjointToAll(VariantBlockStateBuilder.PartialBlockstate newState) {
        return this.coveredStates.stream().noneMatch(newState);
    }

    public VariantBlockStateBuilder forAllStates(Function<BlockState, ConfiguredModel[]> mapper) {
        return this.forAllStatesExcept(mapper);
    }

    public VariantBlockStateBuilder forAllStatesExcept(Function<BlockState, ConfiguredModel[]> mapper, Property<?>... ignored) {
        Set<VariantBlockStateBuilder.PartialBlockstate> seen = new HashSet<>();
        for (BlockState fullState : this.owner.getStateDefinition().getPossibleStates()) {
            Map<Property<?>, Comparable<?>> propertyValues = Maps.newLinkedHashMap(fullState.getValues());
            for (Property<?> p : ignored) {
                propertyValues.remove(p);
            }
            //noinspection ObjectAllocationInLoop
            VariantBlockStateBuilder.PartialBlockstate partialState = new VariantBlockStateBuilder.PartialBlockstate(this.owner,
                                                                                                                     propertyValues,
                                                                                                                     this);
            if (seen.add(partialState)) {
                this.setModels(partialState, mapper.apply(fullState));
            }
        }
        return this;
    }

    public Map<VariantBlockStateBuilder.PartialBlockstate, BlockStateProvider.ConfiguredModelList> getModels() {
        return this.models;
    }

    public Block getOwner() {
        return this.owner;
    }

    public VariantBlockStateBuilder.PartialBlockstate partialState() {
        return new VariantBlockStateBuilder.PartialBlockstate(this.owner, this);
    }

    /**
     * Assign some models to a given {@link net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate partial state},
     * throwing an exception if the state has already been configured. Otherwise,
     * simply calls {@link #addModels(net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate, ConfiguredModel...)}.
     *
     * @param state The {@link net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate partial state} for which to set
     *              the models
     * @param model A set of models to assign to this state
     * @return this builder
     * @throws IllegalArgumentException if {@code state} has already been configured
     * @see #addModels(net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate, ConfiguredModel...)
     */
    public VariantBlockStateBuilder setModels(VariantBlockStateBuilder.PartialBlockstate state, ConfiguredModel... model) {
        Preconditions.checkArgument(!this.models.containsKey(state), "Cannot set models for a state that has already been configured: %s", state);
        this.addModels(state, model);
        return this;
    }

    @Override
    public JsonObject toJson() {
        List<BlockState> missingStates = Lists.newArrayList(this.owner.getStateDefinition().getPossibleStates());
        missingStates.removeAll(this.coveredStates);
        Preconditions.checkState(missingStates.isEmpty(), "Blockstate for block %s does not cover all states. Missing: %s", this.owner,
                                 missingStates);
        JsonObject variants = new JsonObject();
        this.getModels().entrySet().stream()
            .sorted(Map.Entry.comparingByKey(VariantBlockStateBuilder.PartialBlockstate.comparingByProperties()))
            .forEach(entry -> variants.add(entry.getKey().toString(), entry.getValue().toJSON()));
        JsonObject main = new JsonObject();
        main.add("variants", variants);
        return main;
    }

    public static class PartialBlockstate implements Predicate<BlockState> {
        @Nullable
        private final VariantBlockStateBuilder outerBuilder;
        private final Block owner;
        private final SortedMap<Property<?>, Comparable<?>> setStates;

        PartialBlockstate(Block owner, @Nullable VariantBlockStateBuilder outerBuilder) {
            this(owner, ImmutableMap.of(), outerBuilder);
        }

        PartialBlockstate(Block owner, Map<Property<?>, Comparable<?>> setStates, @Nullable VariantBlockStateBuilder outerBuilder) {
            this.owner = owner;
            this.outerBuilder = outerBuilder;
            for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
                Property<?> prop = entry.getKey();
                Comparable<?> value = entry.getValue();
                Preconditions.checkArgument(owner.getStateDefinition().getProperties().contains(prop), "Property %s not found on block %s", entry,
                                            this.owner);
                Preconditions.checkArgument(prop.getPossibleValues().contains(value), "%s is not a valid value for %s", value, prop);
            }
            this.setStates = Maps.newTreeMap(Comparator.comparing(Property::getName));
            this.setStates.putAll(setStates);
        }

        @SuppressWarnings("rawtypes")
        public static Comparator<VariantBlockStateBuilder.PartialBlockstate> comparingByProperties() {
            // Sort variants inversely by property values, to approximate vanilla style
            return (s1, s2) -> {
                Set<Property<?>> propUniverse = new TreeSet<>(s1.getSetStates().comparator().reversed());
                propUniverse.addAll(s1.getSetStates().keySet());
                propUniverse.addAll(s2.getSetStates().keySet());
                for (Property<?> prop : propUniverse) {
                    Comparable val1 = s1.getSetStates().get(prop);
                    Comparable val2 = s2.getSetStates().get(prop);
                    if (val1 == null && val2 != null) {
                        return -1;
                    }
                    if (val2 == null && val1 != null) {
                        return 1;
                    }
                    if (val1 != null) {
                        int cmp = val1.compareTo(val2);
                        if (cmp != 0) {
                            return cmp;
                        }
                    }
                }
                return 0;
            };
        }

        /**
         * Add models to the current state's variant. For use when it is more convenient
         * to add multiple sets of models, as a replacement for
         * {@link #setModels(ConfiguredModel...)}.
         *
         * @param models The models to add.
         * @return {@code this}
         * @throws NullPointerException If the parent builder is {@code null}
         */
        public VariantBlockStateBuilder.PartialBlockstate addModels(ConfiguredModel... models) {
            this.checkValidOwner();
            assert this.outerBuilder != null;
            this.outerBuilder.addModels(this, models);
            return this;
        }

        private void checkValidOwner() {
            Preconditions.checkNotNull(this.outerBuilder, "Partial blockstate must have a valid owner to perform this action");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            VariantBlockStateBuilder.PartialBlockstate that = (VariantBlockStateBuilder.PartialBlockstate) o;
            return this.owner.equals(that.owner) && this.setStates.equals(that.setStates);
        }

        public Block getOwner() {
            return this.owner;
        }

        public SortedMap<Property<?>, Comparable<?>> getSetStates() {
            return this.setStates;
        }

        @Override
        public int hashCode() {
            return this.owner.hashCode() * 31 + this.setStates.hashCode();
        }

        /**
         * Creates a builder for models to assign to this state, which when completed
         * via {@link ConfiguredModel.Builder#addModel()} will assign the resultant set
         * of models to this state.
         *
         * @return the model builder
         * @see ConfiguredModel.Builder
         */
        public ConfiguredModel.Builder<VariantBlockStateBuilder> modelForState() {
            this.checkValidOwner();
            assert this.outerBuilder != null;
            return ConfiguredModel.builder(this.outerBuilder, this);
        }

        /**
         * Complete this state without adding any new models, and return a new partial
         * state via the parent builder. For use after calling
         * {@link #addModels(ConfiguredModel...)}.
         *
         * @return A fresh partial state as specified by
         * {@link VariantBlockStateBuilder#partialState()}.
         * @throws NullPointerException If the parent builder is {@code null}
         */
        public VariantBlockStateBuilder.PartialBlockstate partialState() {
            this.checkValidOwner();
            assert this.outerBuilder != null;
            return this.outerBuilder.partialState();
        }

        /**
         * Set this variant's models, and return the parent builder.
         *
         * @param models The models to set
         * @return The parent builder instance
         * @throws NullPointerException If the parent builder is {@code null}
         */
        public VariantBlockStateBuilder setModels(ConfiguredModel... models) {
            this.checkValidOwner();
            assert this.outerBuilder != null;
            return this.outerBuilder.setModels(this, models);
        }

        @Override
        public boolean test(BlockState blockState) {
            if (blockState.getBlock() != this.getOwner()) {
                return false;
            }
            for (Map.Entry<Property<?>, Comparable<?>> entry : this.setStates.entrySet()) {
                if (blockState.getValue(entry.getKey()) != entry.getValue()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            for (Map.Entry<Property<?>, Comparable<?>> entry : this.setStates.entrySet()) {
                if (!ret.isEmpty()) {
                    ret.append(',');
                }
                ret.append(entry.getKey().getName())
                   .append('=')
                   .append(((Property) entry.getKey()).getName(entry.getValue()));
            }
            return ret.toString();
        }

        public <T extends Comparable<T>> VariantBlockStateBuilder.PartialBlockstate with(Property<T> prop, T value) {
            Preconditions.checkArgument(!this.setStates.containsKey(prop), "Property %s has already been set", prop);
            Map<Property<?>, Comparable<?>> newState = new HashMap<>(this.setStates);
            newState.put(prop, value);
            return new VariantBlockStateBuilder.PartialBlockstate(this.owner, newState, this.outerBuilder);
        }
    }
}
