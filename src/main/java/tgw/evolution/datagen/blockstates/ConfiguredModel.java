package tgw.evolution.datagen.blockstates;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraftforge.client.model.generators.ModelFile;
import tgw.evolution.datagen.BlockStateProvider;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ConfiguredModel {
    /**
     * The default random weight of configured models, used by convenience
     * overloads.
     */
    public static final int DEFAULT_WEIGHT = 1;

    public final ModelFile model;
    public final int rotationX;
    public final int rotationY;
    public final boolean uvLock;
    public final int weight;

    /**
     * Construct a new {@link ConfiguredModel}.
     *
     * @param model     the underlying model
     * @param rotationX x-rotation to apply to the model
     * @param rotationY y-rotation to apply to the model
     * @param uvLock    if uvlock should be enabled
     * @param weight    the random weight of the model
     * @throws NullPointerException     if {@code model} is {@code null}
     * @throws IllegalArgumentException if x and/or y rotation are not valid (see
     *                                  {@link BlockModelRotation})
     * @throws IllegalArgumentException if weight is less than or equal to zero
     */
    public ConfiguredModel(ModelFile model, int rotationX, int rotationY, boolean uvLock, int weight) {
        Preconditions.checkNotNull(model);
        this.model = model;
        checkRotation(rotationX, rotationY);
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.uvLock = uvLock;
        checkWeight(weight);
        this.weight = weight;
    }

    /**
     * Construct a new {@link ConfiguredModel} with the {@link #DEFAULT_WEIGHT
     * default random weight}.
     *
     * @param model     the underlying model
     * @param rotationX x-rotation to apply to the model
     * @param rotationY y-rotation to apply to the model
     * @param uvLock    if uvlock should be enabled
     * @throws NullPointerException     if {@code model} is {@code null}
     * @throws IllegalArgumentException if x and/or y rotation are not valid (see
     *                                  {@link BlockModelRotation})
     */
    public ConfiguredModel(ModelFile model, int rotationX, int rotationY, boolean uvLock) {
        this(model, rotationX, rotationY, uvLock, DEFAULT_WEIGHT);
    }

    /**
     * Construct a new {@link ConfiguredModel} with the default rotation (0, 0),
     * uvlock (false), and {@link #DEFAULT_WEIGHT default random weight}.
     *
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public ConfiguredModel(ModelFile model) {
        this(model, 0, 0, false);
    }

    public static ConfiguredModel[] allRotations(ModelFile model, boolean uvlock) {
        return allRotations(model, uvlock, DEFAULT_WEIGHT);
    }

    public static ConfiguredModel[] allRotations(ModelFile model, boolean uvlock, int weight) {
        return validRotations()
                .mapToObj(x -> allYRotations(model, x, uvlock, weight))
                .flatMap(Arrays::stream)
                .toArray(ConfiguredModel[]::new);
    }

    public static ConfiguredModel[] allYRotations(ModelFile model, int x, boolean uvlock) {
        return allYRotations(model, x, uvlock, DEFAULT_WEIGHT);
    }

    public static ConfiguredModel[] allYRotations(ModelFile model, int x, boolean uvlock, int weight) {
        return validRotations()
                .mapToObj(y -> new ConfiguredModel(model, x, y, uvlock, weight))
                .toArray(ConfiguredModel[]::new);
    }

    /**
     * Create a new unowned {@link ConfiguredModel.Builder}.
     *
     * @return the builder
     * @see ConfiguredModel.Builder
     */
    public static ConfiguredModel.Builder<?> builder() {
        return new ConfiguredModel.Builder<>();
    }

    static ConfiguredModel.Builder<VariantBlockStateBuilder> builder(VariantBlockStateBuilder outer,
                                                                     VariantBlockStateBuilder.PartialBlockstate state) {
        return new ConfiguredModel.Builder<>(models -> outer.setModels(state, models), ImmutableList.of());
    }

    static ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> builder(MultiPartBlockStateBuilder outer) {
        return new ConfiguredModel.Builder<>(models -> {
            MultiPartBlockStateBuilder.PartBuilder ret = outer.new PartBuilder(new BlockStateProvider.ConfiguredModelList(models));
            outer.addPart(ret);
            return ret;
        }, ImmutableList.of());
    }

    static void checkRotation(int rotationX, int rotationY) {
        BlockModelRotation.by(rotationX, rotationY);
    }

    static void checkWeight(int weight) {
        Preconditions.checkArgument(weight >= 1, "Model weight must be greater than or equal to 1. Found: %d", weight);
    }

    private static IntStream validRotations() {
        return IntStream.range(0, 4).map(i -> i * 90);
    }

    public JsonObject toJSON(boolean includeWeight) {
        JsonObject modelJson = new JsonObject();
        modelJson.addProperty("model", this.model.getLocation().toString());
        if (this.rotationX != 0) {
            modelJson.addProperty("x", this.rotationX);
        }
        if (this.rotationY != 0) {
            modelJson.addProperty("y", this.rotationY);
        }
        if (this.uvLock) {
            modelJson.addProperty("uvlock", true);
        }
        if (includeWeight && this.weight != DEFAULT_WEIGHT) {
            modelJson.addProperty("weight", this.weight);
        }
        return modelJson;
    }

    /**
     * A builder for {@link net.minecraftforge.client.model.generators.ConfiguredModel}s, which can contain a callback for
     * processing the finished result. If no callback is available (e.g. in the case
     * of {@link net.minecraftforge.client.model.generators.ConfiguredModel#builder()}), some methods will not be available.
     * <p>
     * Multiple models can be configured at once through the use of
     * {@link #nextModel()}.
     *
     * @param <T> the type of the owning builder, which supplied the callback, and
     *            will be returned upon completion.
     */
    public static class Builder<T> {

        @Nullable
        private final Function<ConfiguredModel[], T> callback;
        private final List<ConfiguredModel> otherModels;
        private ModelFile model;
        private int rotationX;
        private int rotationY;
        private boolean uvLock;
        private int weight = DEFAULT_WEIGHT;

        Builder() {
            this(null, ImmutableList.of());
        }

        Builder(@Nullable Function<ConfiguredModel[], T> callback, List<ConfiguredModel> otherModels) {
            this.callback = callback;
            this.otherModels = otherModels;
        }

        /**
         * Apply the contained callback and return the owning builder object. What the
         * callback does is not defined by this class, but most likely it adds the built
         * models to the current variant being configured.
         * <p>
         * Known callbacks include:
         * <ul>
         * <li>{@link VariantBlockStateBuilder.PartialBlockstate#modelForState()}</li>
         * <li>{@link MultiPartBlockStateBuilder#part()}</li>
         * </ul>
         *
         * @return the owning builder object
         * @throws NullPointerException if there is no owning builder (and thus no callback)
         */
        public T addModel() {
            Preconditions.checkNotNull(this.callback, "Cannot use addModel() without an owning builder present");
            return this.callback.apply(this.build());
        }

        /**
         * Build all configured models and return them as an array.
         *
         * @return the array of built models.
         */
        public ConfiguredModel[] build() {
            return ObjectArrays.concat(this.otherModels.toArray(new ConfiguredModel[this.otherModels.size()]), this.buildLast());
        }

        /**
         * Build the most recent model, as if {@link #nextModel()} was never called.
         * Useful for single-model builders.
         *
         * @return the most recently configured model
         */
        public ConfiguredModel buildLast() {
            return new ConfiguredModel(this.model, this.rotationX, this.rotationY, this.uvLock,
                                       this.weight);
        }

        /**
         * Set the underlying model object for this configured model.
         *
         * @param model the model
         * @return this builder
         * @throws NullPointerException if {@code model} is {@code null}
         */
        public ConfiguredModel.Builder<T> modelFile(ModelFile model) {
            Preconditions.checkNotNull(model, "Model must not be null");
            this.model = model;
            return this;
        }

        /**
         * Complete the current model and return a new builder instance with the same
         * callback, and storing all previously built models.
         *
         * @return a new builder for configuring the next model
         */
        public ConfiguredModel.Builder<T> nextModel() {
            return new ConfiguredModel.Builder<>(this.callback, Arrays.asList(this.build()));
        }

        /**
         * Set the x-rotation for this model.
         *
         * @param value the x-rotation value
         * @return this builder
         * @throws IllegalArgumentException if {@code value} is not a valid x-rotation
         *                                  (see {@link BlockModelRotation})
         */
        public ConfiguredModel.Builder<T> rotationX(int value) {
            checkRotation(value, this.rotationY);
            this.rotationX = value;
            return this;
        }

        /**
         * Set the y-rotation for this model.
         *
         * @param value the y-rotation value
         * @return this builder
         * @throws IllegalArgumentException if {@code value} is not a valid y-rotation
         *                                  (see {@link BlockModelRotation})
         */
        public ConfiguredModel.Builder<T> rotationY(int value) {
            checkRotation(this.rotationX, value);
            this.rotationY = value;
            return this;
        }

        public ConfiguredModel.Builder<T> uvLock(boolean value) {
            this.uvLock = value;
            return this;
        }

        /**
         * Set the random weight for this model.
         *
         * @param value the weight value
         * @return this builder
         * @throws IllegalArgumentException if {@code value} is less than or equal to
         *                                  zero
         */
        public ConfiguredModel.Builder<T> weight(int value) {
            checkWeight(value);
            this.weight = value;
            return this;
        }
    }
}
