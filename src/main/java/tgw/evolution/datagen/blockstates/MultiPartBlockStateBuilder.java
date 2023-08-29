package tgw.evolution.datagen.blockstates;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.datagen.BlockStateProvider;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MultiPartBlockStateBuilder implements IGeneratedBlockstate {

    private final Block owner;
    private final OList<MultiPartBlockStateBuilder.PartBuilder> parts = new OArrayList<>();

    public MultiPartBlockStateBuilder(Block owner) {
        this.owner = owner;
    }

    private static JsonObject toJson(List<MultiPartBlockStateBuilder.PartBuilder.ConditionGroup> conditions, boolean useOr) {
        JsonObject groupJson = new JsonObject();
        JsonArray innerGroupJson = new JsonArray();
        groupJson.add(useOr ? "OR" : "AND", innerGroupJson);
        for (MultiPartBlockStateBuilder.PartBuilder.ConditionGroup group : conditions) {
            innerGroupJson.add(group.toJson());
        }
        return groupJson;
    }

    private static JsonObject toJson(Multimap<Property<?>, Comparable<?>> conditions, boolean useOr) {
        JsonObject groupJson = new JsonObject();
        StringBuilder activeString = new StringBuilder();
        for (Map.Entry<Property<?>, Collection<Comparable<?>>> e : conditions.asMap().entrySet()) {
            activeString.setLength(0);
            for (Comparable<?> val : e.getValue()) {
                if (!activeString.isEmpty()) {
                    activeString.append("|");
                }
                activeString.append(((Property) e.getKey()).getName(val));
            }
            groupJson.addProperty(e.getKey().getName(), activeString.toString());
        }
        if (useOr) {
            JsonArray innerWhen = new JsonArray();
            for (Map.Entry<String, JsonElement> entry : groupJson.entrySet()) {
                //noinspection ObjectAllocationInLoop
                JsonObject obj = new JsonObject();
                obj.add(entry.getKey(), entry.getValue());
                innerWhen.add(obj);
            }
            groupJson = new JsonObject();
            groupJson.add("OR", innerWhen);
        }
        return groupJson;
    }

    MultiPartBlockStateBuilder addPart(MultiPartBlockStateBuilder.PartBuilder part) {
        this.parts.add(part);
        return this;
    }

    /**
     * Creates a builder for models to assign to a {@link MultiPartBlockStateBuilder.PartBuilder}, which when
     * completed via {@link ConfiguredModel.Builder#addModel()} will assign the
     * resultant set of models to the part and return it for further processing.
     *
     * @return the model builder
     * @see ConfiguredModel.Builder
     */
    public ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> part() {
        return ConfiguredModel.builder(this);
    }

    @Override
    public JsonObject toJson() {
        JsonArray variants = new JsonArray();
        for (MultiPartBlockStateBuilder.PartBuilder part : this.parts) {
            variants.add(part.toJson());
        }
        JsonObject main = new JsonObject();
        main.add("multipart", variants);
        return main;
    }

    public class PartBuilder {
        protected final Multimap<Property<?>, Comparable<?>> conditions = MultimapBuilder.linkedHashKeys().arrayListValues().build();
        protected final OList<MultiPartBlockStateBuilder.PartBuilder.ConditionGroup> nestedConditionGroups = new OArrayList<>();
        protected BlockStateProvider.ConfiguredModelList models;
        protected boolean useOr;

        PartBuilder(BlockStateProvider.ConfiguredModelList models) {
            this.models = models;
        }

        public boolean canApplyTo(Block b) {
            return b.getStateDefinition().getProperties().containsAll(this.conditions.keySet());
        }

        /**
         * Set a condition for this part, which consists of a property and a set of
         * valid values. Can be called multiple times for multiple different properties.
         *
         * @param <T>    the type of the property value
         * @param prop   the property
         * @param values a set of valid values
         * @return this builder
         * @throws NullPointerException     if {@code prop} is {@code null}
         * @throws NullPointerException     if {@code values} is {@code null}
         * @throws IllegalArgumentException if {@code values} is empty
         * @throws IllegalArgumentException if {@code prop} has already been configured
         * @throws IllegalArgumentException if {@code prop} is not applicable to the
         *                                  current block's state
         * @throws IllegalStateException    if {@code !nestedConditionGroups.isEmpty()}
         */
        @SafeVarargs
        public final <T extends Comparable<T>> MultiPartBlockStateBuilder.PartBuilder condition(Property<T> prop, T... values) {
            Preconditions.checkNotNull(prop, "Property must not be null");
            Preconditions.checkNotNull(values, "Value list must not be null");
            Preconditions.checkArgument(values.length > 0, "Value list must not be empty");
            Preconditions.checkArgument(!this.conditions.containsKey(prop), "Cannot set condition for property \"%s\" more than once",
                                        prop.getName());
            Preconditions.checkArgument(this.canApplyTo(MultiPartBlockStateBuilder.this.owner), "IProperty %s is not valid for the block %s", prop,
                                        MultiPartBlockStateBuilder.this.owner);
            Preconditions.checkState(this.nestedConditionGroups.isEmpty(),
                                     "Can't have normal conditions if there are already nested condition groups");
            this.conditions.putAll(prop, Arrays.asList(values));
            return this;
        }

        public MultiPartBlockStateBuilder end() {return MultiPartBlockStateBuilder.this;}

        /**
         * Allows having nested groups of conditions if there are not any normal conditions.
         *
         * @throws IllegalStateException if {@code !conditions.isEmpty()}
         */
        public final MultiPartBlockStateBuilder.PartBuilder.ConditionGroup nestedGroup() {
            Preconditions.checkState(this.conditions.isEmpty(), "Can't have nested condition groups if there are already normal conditions");
            MultiPartBlockStateBuilder.PartBuilder.ConditionGroup group = new MultiPartBlockStateBuilder.PartBuilder.ConditionGroup();
            this.nestedConditionGroups.add(group);
            return group;
        }

        JsonObject toJson() {
            JsonObject out = new JsonObject();
            if (!this.conditions.isEmpty()) {
                out.add("when", MultiPartBlockStateBuilder.toJson(this.conditions, this.useOr));
            }
            else if (!this.nestedConditionGroups.isEmpty()) {
                out.add("when", MultiPartBlockStateBuilder.toJson(this.nestedConditionGroups, this.useOr));
            }
            out.add("apply", this.models.toJSON());
            return out;
        }

        /**
         * Makes this part get applied if any of the conditions/condition groups are true, instead of all of them needing to be true.
         */
        public MultiPartBlockStateBuilder.PartBuilder useOr() {
            this.useOr = true;
            return this;
        }

        public class ConditionGroup {
            protected final Multimap<Property<?>, Comparable<?>> conditions = MultimapBuilder.linkedHashKeys().arrayListValues().build();
            protected final OList<ConditionGroup> nestedConditionGroups = new OArrayList<>();
            protected boolean useOr;
            private @Nullable MultiPartBlockStateBuilder.PartBuilder.ConditionGroup parent;

            /**
             * Set a condition for this part, which consists of a property and a set of
             * valid values. Can be called multiple times for multiple different properties.
             *
             * @param <T>    the type of the property value
             * @param prop   the property
             * @param values a set of valid values
             * @return this builder
             * @throws NullPointerException     if {@code prop} is {@code null}
             * @throws NullPointerException     if {@code values} is {@code null}
             * @throws IllegalArgumentException if {@code values} is empty
             * @throws IllegalArgumentException if {@code prop} has already been configured
             * @throws IllegalArgumentException if {@code prop} is not applicable to the
             *                                  current block's state
             * @throws IllegalStateException    if {@code !nestedConditionGroups.isEmpty()}
             */
            @SafeVarargs
            public final <T extends Comparable<T>> MultiPartBlockStateBuilder.PartBuilder.ConditionGroup condition(Property<T> prop, T... values) {
                Preconditions.checkNotNull(prop, "Property must not be null");
                Preconditions.checkNotNull(values, "Value list must not be null");
                Preconditions.checkArgument(values.length > 0, "Value list must not be empty");
                Preconditions.checkArgument(!this.conditions.containsKey(prop), "Cannot set condition for property \"%s\" more than once",
                                            prop.getName());
                Preconditions.checkArgument(PartBuilder.this.canApplyTo(MultiPartBlockStateBuilder.this.owner),
                                            "IProperty %s is not valid for the block %s", prop,
                                            MultiPartBlockStateBuilder.this.owner);
                Preconditions.checkState(this.nestedConditionGroups.isEmpty(),
                                         "Can't have normal conditions if there are already nested condition groups");
                this.conditions.putAll(prop, Arrays.asList(values));
                return this;
            }

            /**
             * Ends this condition group and returns the part builder
             *
             * @throws IllegalStateException If this is a nested condition group
             */
            public MultiPartBlockStateBuilder.PartBuilder end() {
                //noinspection VariableNotUsedInsideIf
                if (this.parent != null) {
                    throw new IllegalStateException("This is a nested condition group, use endNestedGroup() instead");
                }
                return MultiPartBlockStateBuilder.PartBuilder.this;
            }

            /**
             * Ends this nested condition group and returns the parent condition group
             *
             * @throws IllegalStateException If this is not a nested condition group
             */
            public MultiPartBlockStateBuilder.PartBuilder.ConditionGroup endNestedGroup() {
                if (this.parent == null) {
                    throw new IllegalStateException("This condition group is not nested, use end() instead");
                }
                return this.parent;
            }

            /**
             * Allows having nested groups of conditions if there are not any normal conditions.
             *
             * @throws IllegalStateException if {@code !conditions.isEmpty()}
             */
            public MultiPartBlockStateBuilder.PartBuilder.ConditionGroup nestedGroup() {
                Preconditions.checkState(this.conditions.isEmpty(), "Can't have nested condition groups if there are already normal conditions");
                MultiPartBlockStateBuilder.PartBuilder.ConditionGroup group = new MultiPartBlockStateBuilder.PartBuilder.ConditionGroup();
                group.parent = this;
                this.nestedConditionGroups.add(group);
                return group;
            }

            JsonObject toJson() {
                if (!this.conditions.isEmpty()) {
                    return MultiPartBlockStateBuilder.toJson(this.conditions, this.useOr);
                }
                if (!this.nestedConditionGroups.isEmpty()) {
                    return MultiPartBlockStateBuilder.toJson(this.nestedConditionGroups, this.useOr);
                }
                return new JsonObject();
            }

            /**
             * Makes this part get applied if any of the conditions/condition groups are true, instead of all of them needing to be true.
             */
            public MultiPartBlockStateBuilder.PartBuilder.ConditionGroup useOr() {
                this.useOr = true;
                return this;
            }
        }
    }
}
