package tgw.evolution.datagen.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.datagen.ModelProvider;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.Enum2OMap;
import tgw.evolution.util.collection.maps.R2OMap;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ModelBuilder<T extends ModelBuilder<T>> extends ModelFile {

    private static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0F, 0.0F, 0.0F);
    private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0F, 0.0F, 0.0F);
    private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
    protected boolean ambientOcclusion = true;
    protected @Nullable CustomLoaderBuilder customLoader;
    protected final OList<ElementBuilder> elements = new OArrayList<>();
    protected final ExistingFileHelper existingFileHelper;
    protected @Nullable BlockModel.GuiLight guiLight;
    protected @Nullable ModelFile parent;
    protected final Map<String, String> textures = new LinkedHashMap<>();
    protected final TransformsBuilder transforms = new TransformsBuilder();

    protected ModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation);
        this.existingFileHelper = existingFileHelper;
    }

    private static String name(ItemTransforms.TransformType type) {
        return switch (type) {
            case FIRST_PERSON_LEFT_HAND -> "firstperson_lefthand";
            case FIRST_PERSON_RIGHT_HAND -> "firstperson_righthand";
            case THIRD_PERSON_LEFT_HAND -> "thirdperson_lefthand";
            case THIRD_PERSON_RIGHT_HAND -> "thirdperson_righthand";
            default -> type.name().toLowerCase();
        };
    }

    private static Number serializeFloat(float f) {
        if ((int) f == f) {
            return (int) f;
        }
        return f;
    }

    private static String serializeLocOrKey(String tex) {
        if (tex.charAt(0) == '#') {
            return tex;
        }
        return new ResourceLocation(tex).toString();
    }

    private static JsonArray serializeVector3f(Vector3f vec) {
        JsonArray ret = new JsonArray();
        ret.add(serializeFloat(vec.x()));
        ret.add(serializeFloat(vec.y()));
        ret.add(serializeFloat(vec.z()));
        return ret;
    }

    public T ao(boolean ao) {
        this.ambientOcclusion = ao;
        return this.self();
    }

    /**
     * Use a custom loader instead of the vanilla elements.
     *
     * @param customLoaderFactory function that returns the custom loader to set, given this and the {@link #existingFileHelper}
     * @return the custom loader builder
     */
    public <L extends CustomLoaderBuilder<T>> L customLoader(BiFunction<T, ExistingFileHelper, L> customLoaderFactory) {
        Preconditions.checkState(this.elements.isEmpty(), "Cannot use elements and custom loaders at the same time");
        Preconditions.checkNotNull(customLoaderFactory, "customLoaderFactory must not be null");
        L customLoader = customLoaderFactory.apply(this.self(), this.existingFileHelper);
        this.customLoader = customLoader;
        return customLoader;
    }

    public ElementBuilder element() {
        Preconditions.checkState(this.customLoader == null, "Cannot use elements and custom loaders at the same time");
        ElementBuilder ret = new ElementBuilder();
        this.elements.add(ret);
        return ret;
    }

    /**
     * Get an existing element builder
     *
     * @param index the index of the existing element builder
     * @return the element builder
     * @throws IndexOutOfBoundsException if {@code} index is out of bounds
     */
    public ElementBuilder element(int index) {
        Preconditions.checkState(this.customLoader == null, "Cannot use elements and custom loaders at the same time");
        Preconditions.checkElementIndex(index, this.elements.size(), "Element index");
        return this.elements.get(index);
    }

    /**
     * Gets the number of elements in this model builder
     *
     * @return the number of elements in this model builder
     */
    public int getElementCount() {
        return this.elements.size();
    }

    public T guiLight(BlockModel.GuiLight light) {
        this.guiLight = light;
        return this.self();
    }

    /**
     * Set the parent model for the current model.
     *
     * @param parent the parent model
     * @return this builder
     * @throws NullPointerException  if {@code parent} is {@code null}
     * @throws IllegalStateException if {@code parent} does not {@link ModelFile#assertExistence() exist}
     */
    public T parent(ModelFile parent) {
        Preconditions.checkNotNull(parent, "Parent must not be null");
        parent.assertExistence();
        this.parent = parent;
        return this.self();
    }

    /**
     * Set the texture for a given dictionary key.
     *
     * @param key     the texture key
     * @param texture the texture, can be another key e.g. {@code "#all"}
     * @return this builder
     * @throws NullPointerException  if {@code key} is {@code null}
     * @throws NullPointerException  if {@code texture} is {@code null}
     * @throws IllegalStateException if {@code texture} is not a key (does not start
     *                               with {@code '#'}) and does not exist in any
     *                               known resource pack
     */
    public T texture(String key, String texture) {
        Preconditions.checkNotNull(key, "Key must not be null");
        Preconditions.checkNotNull(texture, "Texture must not be null");
        if (texture.charAt(0) == '#') {
            this.textures.put(key, texture);
            return this.self();
        }
        ResourceLocation asLoc;
        if (texture.contains(":")) {
            asLoc = new ResourceLocation(texture);
        }
        else {
            asLoc = new ResourceLocation(this.getLocation().getNamespace(), texture);
        }
        return this.texture(key, asLoc);
    }

    /**
     * Set the texture for a given dictionary key.
     *
     * @param key     the texture key
     * @param texture the texture
     * @return this builder
     * @throws NullPointerException  if {@code key} is {@code null}
     * @throws NullPointerException  if {@code texture} is {@code null}
     * @throws IllegalStateException if {@code texture} is not a key (does not start
     *                               with {@code '#'}) and does not exist in any
     *                               known resource pack
     */
    public T texture(String key, ResourceLocation texture) {
        if (!this.existingFileHelper.exists(texture, ModelProvider.TEXTURE)) {
            Evolution.warn("Texture {} does not exist in any known resource pack", texture);
        }
        this.textures.put(key, texture.toString());
        return this.self();
    }

    @SuppressWarnings("ObjectAllocationInLoop")
    @VisibleForTesting
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        if (this.parent != null) {
            root.addProperty("parent", this.parent.getLocation().toString());
        }
        if (!this.ambientOcclusion) {
            root.addProperty("ambientocclusion", false);
        }
        if (this.guiLight != null) {
            root.addProperty("gui_light", this.guiLight.name().toLowerCase());
        }
        Map<ItemTransforms.TransformType, ItemTransform> transforms = this.transforms.build();
        if (!transforms.isEmpty()) {
            JsonObject display = new JsonObject();
            for (Map.Entry<ItemTransforms.TransformType, ItemTransform> e : transforms.entrySet()) {
                JsonObject transform = new JsonObject();
                ItemTransform vec = e.getValue();
                if (vec.equals(ItemTransform.NO_TRANSFORM)) {
                    continue;
                }
                if (!vec.rotation.equals(DEFAULT_ROTATION)) {
                    transform.add("rotation", serializeVector3f(vec.rotation));
                }
                if (!vec.translation.equals(DEFAULT_TRANSLATION)) {
                    transform.add("translation", serializeVector3f(e.getValue().translation));
                }
                if (!vec.scale.equals(DEFAULT_SCALE)) {
                    transform.add("scale", serializeVector3f(e.getValue().scale));
                }
                display.add(name(e.getKey()), transform);
            }
            root.add("display", display);
        }
        if (!this.textures.isEmpty()) {
            JsonObject textures = new JsonObject();
            for (Map.Entry<String, String> e : this.textures.entrySet()) {
                textures.addProperty(e.getKey(), serializeLocOrKey(e.getValue()));
            }
            root.add("textures", textures);
        }
        if (!this.elements.isEmpty()) {
            JsonArray elements = new JsonArray();
            this.elements.stream().map(ElementBuilder::build).forEach(part -> {
                JsonObject partObj = new JsonObject();
                partObj.add("from", serializeVector3f(part.from));
                partObj.add("to", serializeVector3f(part.to));
                if (part.rotation != null) {
                    JsonObject rotation = new JsonObject();
                    rotation.add("origin", serializeVector3f(part.rotation.origin));
                    rotation.addProperty("axis", part.rotation.axis.getSerializedName());
                    rotation.addProperty("angle", part.rotation.angle);
                    if (part.rotation.rescale) {
                        rotation.addProperty("rescale", true);
                    }
                    partObj.add("rotation", rotation);
                }
                if (!part.shade) {
                    partObj.addProperty("shade", false);
                }
                JsonObject faces = new JsonObject();
                for (Direction dir : DirectionUtil.ALL) {
                    BlockElementFace face = part.faces.get(dir);
                    if (face == null) {
                        continue;
                    }
                    JsonObject faceObj = new JsonObject();
                    faceObj.addProperty("texture", serializeLocOrKey(face.texture));
                    if (!Arrays.equals(face.uv.uvs, part.uvsByFace(dir))) {
                        faceObj.add("uv", new Gson().toJsonTree(face.uv.uvs));
                    }
                    if (face.cullForDirection != null) {
                        faceObj.addProperty("cullface", face.cullForDirection.getSerializedName());
                    }
                    if (face.uv.rotation != 0) {
                        faceObj.addProperty("rotation", face.uv.rotation);
                    }
                    if (face.tintIndex != -1) {
                        faceObj.addProperty("tintindex", face.tintIndex);
                    }
                    faces.add(dir.getSerializedName(), faceObj);
                }
                if (!part.faces.isEmpty()) {
                    partObj.add("faces", faces);
                }
                elements.add(partObj);
            });
            root.add("elements", elements);
        }
        if (this.customLoader != null) {
            return this.customLoader.toJson(root);
        }
        return root;
    }

    public TransformsBuilder transforms() {
        return this.transforms;
    }

    @Override
    protected boolean exists() {
        return true;
    }

    private T self() {
        return (T) this;
    }

    public class ElementBuilder {

        private final R2OMap<Direction, FaceBuilder> faces = new Enum2OMap<>(Direction.class);
        private final Vector3f from = new Vector3f();
        private @Nullable RotationBuilder rotation;
        private boolean shade = true;
        private final Vector3f to = new Vector3f(16, 16, 16);

        private static void validateCoordinate(float coord, char name) {
            Preconditions.checkArgument(!(coord < -16.0F) && !(coord > 32.0F), "Position " + name + " out of range, must be within [-16, 32]. Found: %d", coord);
        }

        private static void validatePosition(Vector3f pos) {
            validateCoordinate(pos.x(), 'x');
            validateCoordinate(pos.y(), 'y');
            validateCoordinate(pos.z(), 'z');
        }

        /**
         * Modify all <em>possible</em> faces dynamically using a function, creating new
         * faces as necessary.
         *
         * @param action the function to apply to each direction
         * @return this builder
         * @throws NullPointerException if {@code action} is {@code null}
         */
        public ElementBuilder allFaces(BiConsumer<Direction, FaceBuilder> action) {
            for (Direction dir : DirectionUtil.ALL) {
                action.accept(dir, this.face(dir));
            }
            return this;
        }

        /**
         * Create a typical cube element, creating new faces as needed, applying the
         * given texture, and setting the cullface.
         *
         * @param texture the texture
         * @return this builder
         * @throws NullPointerException if {@code texture} is {@code null}
         */
        public ElementBuilder cube(String texture) {
            return this.allFaces(this.addTexture(texture).andThen((dir, f) -> f.cullface(dir)));
        }

        public T end() {
            return ModelBuilder.this.self();
        }

        /**
         * Return or create the face builder for the given direction.
         *
         * @param dir the direction
         * @return the face builder for the given direction
         */
        public FaceBuilder face(Direction dir) {
            FaceBuilder faceBuilder = this.faces.get(dir);
            if (faceBuilder == null) {
                faceBuilder = new FaceBuilder();
                this.faces.put(dir, faceBuilder);
            }
            return faceBuilder;
        }

        /**
         * Modify all <em>existing</em> faces dynamically using a function.
         *
         * @param action the function to apply to each direction
         * @return this builder
         */
        public ElementBuilder faces(BiConsumer<Direction, FaceBuilder> action) {
            this.faces.forEach(action);
            return this;
        }

        /**
         * Set the "from" position for this element.
         *
         * @param x x-position for this vector
         * @param y y-position for this vector
         * @param z z-position for this vector
         * @return this builder
         * @throws IllegalArgumentException if the vector is out of bounds (any
         *                                  coordinate not between -16 and 32,
         *                                  inclusive)
         */
        public ElementBuilder from(float x, float y, float z) {
            this.from.set(x, y, z);
            validatePosition(this.from);
            return this;
        }

        public RotationBuilder rotation() {
            if (this.rotation == null) {
                this.rotation = new RotationBuilder();
            }
            return this.rotation;
        }

        public ElementBuilder shade(boolean shade) {
            this.shade = shade;
            return this;
        }

        /**
         * Texture all <em>existing</em> faces in the current element with the given
         * texture.
         *
         * @param texture the texture
         * @return this builder
         * @throws NullPointerException if {@code texture} is {@code null}
         */
        public ElementBuilder texture(String texture) {
            return this.faces(this.addTexture(texture));
        }

        /**
         * Texture all <em>possible</em> faces in the current element with the given
         * texture, creating new faces where necessary.
         *
         * @param texture the texture
         * @return this builder
         * @throws NullPointerException if {@code texture} is {@code null}
         */
        public ElementBuilder textureAll(String texture) {
            return this.allFaces(this.addTexture(texture));
        }

        /**
         * Set the "to" position for this element.
         *
         * @param x x-position for this vector
         * @param y y-position for this vector
         * @param z z-position for this vector
         * @return this builder
         * @throws IllegalArgumentException if the vector is out of bounds (any
         *                                  coordinate not between -16 and 32,
         *                                  inclusive)
         */
        public ElementBuilder to(float x, float y, float z) {
            this.to.set(x, y, z);
            validatePosition(this.to);
            return this;
        }

        BlockElement build() {
            R2OMap<Direction, BlockElementFace> faces;
            var thisFaces = this.faces;
            if (thisFaces.isEmpty()) {
                faces = R2OMap.emptyMap();
            }
            else {
                faces = new Enum2OMap<>(Direction.class);
                for (long it = thisFaces.beginIteration(); thisFaces.hasNextIteration(it); it = thisFaces.nextEntry(it)) {
                    faces.put(thisFaces.getIterationKey(it), thisFaces.getIterationValue(it).build());
                }
            }
            return new BlockElement(this.from, this.to, faces, this.rotation == null ? null : this.rotation.build(), this.shade);
        }

        private BiConsumer<Direction, FaceBuilder> addTexture(String texture) {
            return ($, f) -> f.texture(texture);
        }

        public class FaceBuilder {

            private @Nullable Direction cullface;
            private FaceRotation rotation = FaceRotation.ZERO;
            private String texture = MissingTextureAtlasSprite.getLocation().toString();
            private int tintindex = -1;
            private float[] uvs;

            FaceBuilder() {
            }

            public FaceBuilder cullface(@Nullable Direction dir) {
                this.cullface = dir;
                return this;
            }

            public ElementBuilder end() {
                return ElementBuilder.this;
            }

            /**
             * Set the texture rotation for the current face.
             *
             * @param rot the rotation
             * @return this builder
             * @throws NullPointerException if {@code rot} is {@code null}
             */
            public FaceBuilder rotation(FaceRotation rot) {
                Preconditions.checkNotNull(rot, "Rotation must not be null");
                this.rotation = rot;
                return this;
            }

            /**
             * Set the texture for the current face.
             *
             * @param texture the texture
             * @return this builder
             * @throws NullPointerException if {@code texture} is {@code null}
             */
            public FaceBuilder texture(String texture) {
                this.texture = texture;
                return this;
            }

            public FaceBuilder tintindex(int index) {
                this.tintindex = index;
                return this;
            }

            public FaceBuilder uvs(float u1, float v1, float u2, float v2) {
                this.uvs = new float[]{u1, v1, u2, v2};
                return this;
            }

            BlockElementFace build() {
                return new BlockElementFace(this.cullface, this.tintindex, this.texture, new BlockFaceUV(this.uvs, this.rotation.rotation));
            }
        }

        public class RotationBuilder {

            private float angle;
            private Direction.Axis axis;
            private Vector3f origin;
            private boolean rescale;

            /**
             * @param angle the rotation angle
             * @return this builder
             * @throws IllegalArgumentException if {@code angle} is invalid (not one of 0, +/-22.5, +/-45)
             */
            public RotationBuilder angle(float angle) {
                // Same logic from BlockPart.Deserializer#parseAngle
                Preconditions.checkArgument(angle == 0.0F || Mth.abs(angle) == 22.5F || Mth.abs(angle) == 45.0F, "Invalid rotation %f found, only -45/-22.5/0/22.5/45 allowed", angle);
                this.angle = angle;
                return this;
            }

            /**
             * @param axis the axis of rotation
             * @return this builder
             * @throws NullPointerException if {@code axis} is {@code null}
             */
            public RotationBuilder axis(Direction.Axis axis) {
                Preconditions.checkNotNull(axis, "Axis must not be null");
                this.axis = axis;
                return this;
            }

            public ElementBuilder end() {return ElementBuilder.this;}

            public RotationBuilder origin(float x, float y, float z) {
                this.origin = new Vector3f(x, y, z);
                return this;
            }

            public RotationBuilder rescale(boolean rescale) {
                this.rescale = rescale;
                return this;
            }

            BlockElementRotation build() {
                return new BlockElementRotation(this.origin, this.axis, this.angle, this.rescale);
            }
        }
    }

    public class TransformsBuilder {

        private final Map<ItemTransforms.TransformType, TransformVecBuilder> transforms = new LinkedHashMap<>();

        public T end() {
            return ModelBuilder.this.self();
        }

        public TransformVecBuilder transform(Perspective type) {
            return this.transform(type.vanillaType);
        }

        public TransformVecBuilder transform(ItemTransforms.TransformType type) {
            TransformVecBuilder builder = this.transforms.get(type);
            if (builder == null) {
                builder = new TransformVecBuilder();
                this.transforms.put(type, builder);
            }
            return builder;
        }

        Map<ItemTransforms.TransformType, ItemTransform> build() {
            return this.transforms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().build(), (k1, k2) -> {
                throw new IllegalArgumentException();
            }, LinkedHashMap::new));
        }

        public class TransformVecBuilder {

            private Vector3f rotation = new Vector3f(0.0F, 0.0F, 0.0F);
            private Vector3f scale = new Vector3f(1.0F, 1.0F, 1.0F);
            private Vector3f translation = new Vector3f(0.0F, 0.0F, 0.0F);

            TransformVecBuilder() {
            }

            public TransformsBuilder end() {
                return TransformsBuilder.this;
            }

            public TransformVecBuilder rotation(float x, float y, float z) {
                this.rotation = new Vector3f(x, y, z);
                return this;
            }

            public TransformVecBuilder scale(float sc) {
                return this.scale(sc, sc, sc);
            }

            public TransformVecBuilder scale(float x, float y, float z) {
                this.scale = new Vector3f(x, y, z);
                return this;
            }

            public TransformVecBuilder translation(float x, float y, float z) {
                this.translation = new Vector3f(x, y, z);
                return this;
            }

            ItemTransform build() {
                return new ItemTransform(this.rotation, this.translation, this.scale);
            }
        }
    }

    public enum FaceRotation {
        ZERO(0),
        CLOCKWISE_90(90),
        UPSIDE_DOWN(180),
        COUNTERCLOCKWISE_90(270),
        ;

        final int rotation;

        FaceRotation(int rotation) {
            this.rotation = rotation;
        }
    }

    public enum Perspective {

        THIRDPERSON_RIGHT(ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, "thirdperson_righthand"),
        THIRDPERSON_LEFT(ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, "thirdperson_lefthand"),
        FIRSTPERSON_RIGHT(ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, "firstperson_righthand"),
        FIRSTPERSON_LEFT(ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, "firstperson_lefthand"),
        HEAD(ItemTransforms.TransformType.HEAD, "head"),
        GUI(ItemTransforms.TransformType.GUI, "gui"),
        GROUND(ItemTransforms.TransformType.GROUND, "ground"),
        FIXED(ItemTransforms.TransformType.FIXED, "fixed");

        public final ItemTransforms.TransformType vanillaType;
        final String name;

        Perspective(ItemTransforms.TransformType vanillaType, String name) {
            this.vanillaType = vanillaType;
            this.name = name;
        }
    }
}
