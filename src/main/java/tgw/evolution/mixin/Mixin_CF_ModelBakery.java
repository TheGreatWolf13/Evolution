package tgw.evolution.mixin;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.*;
import tgw.evolution.util.collection.sets.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mixin(ModelBakery.class)
public abstract class Mixin_CF_ModelBakery {

    @Shadow @Final public static ModelResourceLocation MISSING_MODEL_LOCATION;
    @Shadow @Final public static BlockModel GENERATION_MARKER;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private static Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS;
    @Shadow @Final private static String MISSING_MODEL_LOCATION_STRING;
    @Shadow @Final private static Set<Material> UNREFERENCED_TEXTURES;
    @Shadow @Final private static ItemModelGenerator ITEM_MODEL_GENERATOR;
    @Shadow @Final @DeleteField private Map<ResourceLocation, Pair<TextureAtlas, TextureAtlas.Preparations>> atlasPreparations;
    @Unique private final O2OMap<ResourceLocation, Pair<TextureAtlas, TextureAtlas.Preparations>> atlasPreparations_;
    @Shadow private @Nullable AtlasSet atlasSet;
    @Shadow @Final @DeleteField private Map<Triple<ResourceLocation, Transformation, Boolean>, BakedModel> bakedCache;
    @Unique private final O2OMap<Triple<ResourceLocation, Transformation, Boolean>, BakedModel> bakedCache_;
    @Shadow @Final @DeleteField private Map<ResourceLocation, BakedModel> bakedTopLevelModels;
    @Unique private final O2OMap<ResourceLocation, BakedModel> bakedTopLevelModels_;
    @Mutable @Shadow @Final @RestoreFinal private BlockColors blockColors;
    @Mutable @Shadow @Final @RestoreFinal private BlockModelDefinition.Context context;
    @Shadow @Final @DeleteField private Set<ResourceLocation> loadingStack;
    @Unique private final OSet<ResourceLocation> loadingStack_;
    @Shadow @Final @DeleteField private Object2IntMap<BlockState> modelGroups;
    @Unique private final O2IMap<BlockState> modelGroups_;
    @Mutable @Shadow @Final @RestoreFinal private ResourceManager resourceManager;
    @Shadow @Final @DeleteField private Map<ResourceLocation, UnbakedModel> topLevelModels;
    @Unique private final O2OMap<ResourceLocation, UnbakedModel> topLevelModels_;
    @Shadow @Final @DeleteField private Map<ResourceLocation, UnbakedModel> unbakedCache;
    @Unique private final O2OMap<ResourceLocation, UnbakedModel> unbakedCache_;

    @ModifyConstructor
    public Mixin_CF_ModelBakery(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profilerFiller, int i) {
        this.loadingStack_ = new OHashSet<>();
        this.context = new BlockModelDefinition.Context();
        this.unbakedCache_ = new O2OHashMap<>();
        this.bakedCache_ = new O2OHashMap<>();
        this.topLevelModels_ = new O2OHashMap<>();
        this.bakedTopLevelModels_ = new O2OHashMap<>();
        this.modelGroups_ = new O2IHashMap<>();
        this.modelGroups_.defaultReturnValue(-1);
        this.resourceManager = resourceManager;
        this.blockColors = blockColors;
        profilerFiller.push("missing_model");
        try {
            this.unbakedCache_.put(MISSING_MODEL_LOCATION, this.loadBlockModel(MISSING_MODEL_LOCATION));
            this.loadTopLevel(MISSING_MODEL_LOCATION);
        }
        catch (IOException var12) {
            LOGGER.error("Error loading missing model, should never happen :(", var12);
            throw new RuntimeException(var12);
        }
        profilerFiller.popPush("static_definitions");
        for (Map.Entry<ResourceLocation, StateDefinition<Block, BlockState>> entry : STATIC_DEFINITIONS.entrySet()) {
            OList<BlockState> possibleStates = entry.getValue().getPossibleStates_();
            for (int j = 0, len = possibleStates.size(); j < len; ++j) {
                this.loadTopLevel(BlockModelShaper.stateToModelLocation(entry.getKey(), possibleStates.get(j)));
            }
        }
        profilerFiller.popPush("blocks");
        for (Block block : Registry.BLOCK) {
            OList<BlockState> possibleStates = block.getStateDefinition().getPossibleStates_();
            for (int j = 0, len = possibleStates.size(); j < len; ++j) {
                this.loadTopLevel(BlockModelShaper.stateToModelLocation(possibleStates.get(j)));

            }
        }
        profilerFiller.popPush("items");
        for (ResourceLocation resourceLocation : Registry.ITEM.keySet()) {
            this.loadTopLevel(new ModelResourceLocation(resourceLocation, "inventory"));
        }
        profilerFiller.popPush("special");
        this.loadTopLevel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
        this.loadTopLevel(new ModelResourceLocation("minecraft:spyglass_in_hand#inventory"));
        profilerFiller.popPush("textures");
        OSet<Pair<String, String>> set = new OLinkedHashSet<>();
        OSet<Material> set2 = new OHashSet<>();
        O2OMap<ResourceLocation, UnbakedModel> topLevelModels = this.topLevelModels_;
        for (long it = topLevelModels.beginIteration(); topLevelModels.hasNextIteration(it); it = topLevelModels.nextEntry(it)) {
            set2.addAll(topLevelModels.getIterationValue(it).getMaterials(this::getModel, set));
        }
        set2.addAll(UNREFERENCED_TEXTURES);
        for (long it = set.beginIteration(); set.hasNextIteration(it); it = set.nextEntry(it)) {
            Pair<String, String> pair = set.getIteration(it);
            String name = pair.getSecond();
            if (!name.equals(MISSING_MODEL_LOCATION_STRING)) {
                LOGGER.warn("Unable to resolve texture reference: {} in {}", pair.getFirst(), name);
            }
        }
        O2OMap<ResourceLocation, OList<Material>> map = new O2OHashMap<>();
        for (long it = set2.beginIteration(); set2.hasNextIteration(it); it = set2.nextEntry(it)) {
            Material material = set2.getIteration(it);
            OList<Material> materials = map.get(material.atlasLocation());
            if (materials == null) {
                materials = new OArrayList<>();
                map.put(material.atlasLocation(), materials);
            }
            materials.add(material);
        }
        profilerFiller.popPush("stitching");
        this.atlasPreparations_ = new O2OHashMap<>();
        for (long it = map.beginIteration(); map.hasNextIteration(it); it = map.nextEntry(it)) {
            ResourceLocation key = map.getIterationKey(it);
            TextureAtlas textureAtlas = new TextureAtlas(key);
            TextureAtlas.Preparations preparations = textureAtlas.prepareToStitch(this.resourceManager, map.getIterationValue(it).stream().map(Material::texture), profilerFiller, i);
            this.atlasPreparations_.put(key, Pair.of(textureAtlas, preparations));
        }
        profilerFiller.pop();
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public static @Nullable <T extends Comparable<T>> T getValueHelper(Property<T> property, String name) {
        return (T) property.getValue_(name);
    }

    @Shadow
    private static Predicate<BlockState> predicate(StateDefinition<Block, BlockState> stateDefinition, String string) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public @Nullable BakedModel bake(ResourceLocation resourceLocation, ModelState modelState) {
        Triple<ResourceLocation, Transformation, Boolean> triple = Triple.of(resourceLocation, modelState.getRotation(), modelState.isUvLocked());
        if (this.bakedCache_.containsKey(triple)) {
            return this.bakedCache_.get(triple);
        }
        if (this.atlasSet == null) {
            throw new IllegalStateException("bake called too early");
        }
        UnbakedModel unbakedModel = this.getModel(resourceLocation);
        if (unbakedModel instanceof BlockModel blockModel) {
            if (blockModel.getRootModel() == GENERATION_MARKER) {
                BlockModel model = ITEM_MODEL_GENERATOR.generateBlockModel(this.atlasSet::getSprite, blockModel);
                return model.bake((ModelBakery) (Object) this, blockModel, this.atlasSet::getSprite, modelState, resourceLocation, false);
            }
        }
        BakedModel bakedModel = unbakedModel.bake((ModelBakery) (Object) this, this.atlasSet::getSprite, modelState, resourceLocation);
        this.bakedCache_.put(triple, bakedModel);
        return bakedModel;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Map<ResourceLocation, BakedModel> getBakedTopLevelModels() {
        return this.bakedTopLevelModels_;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public UnbakedModel getModel(ResourceLocation location) {
        if (this.unbakedCache_.containsKey(location)) {
            return this.unbakedCache_.get(location);
        }
        if (this.loadingStack_.contains(location)) {
            throw new IllegalStateException("Circular reference while loading " + location);
        }
        this.loadingStack_.add(location);
        UnbakedModel unbakedmodel = this.unbakedCache_.get(MISSING_MODEL_LOCATION);
        while (!this.loadingStack_.isEmpty()) {
            ResourceLocation loc = this.loadingStack_.getSampleElement();
            assert loc != null;
            try {
                if (!this.unbakedCache_.containsKey(loc)) {
                    this.loadModel(loc);
                }
            }
            catch (ModelBakery.BlockStateDefinitionException e) {
                LOGGER.warn(e.getMessage());
                this.unbakedCache_.put(loc, unbakedmodel);
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", loc, location, exception);
                this.unbakedCache_.put(loc, unbakedmodel);
            }
            finally {
                this.loadingStack_.remove(loc);
            }
        }
        return this.unbakedCache_.getOrDefault(location, unbakedmodel);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Object2IntMap<BlockState> getModelGroups() {
        return this.modelGroups_;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public AtlasSet uploadTextures(TextureManager textureManager, ProfilerFiller profiler) {
        profiler.push("atlas");
        O2OMap<ResourceLocation, Pair<TextureAtlas, TextureAtlas.Preparations>> atlasPreparations = this.atlasPreparations_;
        OList<TextureAtlas> atlases = new OArrayList<>();
        for (long it = atlasPreparations.beginIteration(); atlasPreparations.hasNextIteration(it); it = atlasPreparations.nextEntry(it)) {
            Pair<TextureAtlas, TextureAtlas.Preparations> pair = atlasPreparations.getIterationValue(it);
            TextureAtlas textureAtlas = pair.getFirst();
            TextureAtlas.Preparations preparations = pair.getSecond();
            textureAtlas.reload(preparations);
            textureManager.register(textureAtlas.location(), textureAtlas);
            textureManager.bindForSetup(textureAtlas.location());
            textureAtlas.updateFilter(preparations);
            atlases.add(textureAtlas);
        }
        this.atlasSet = new AtlasSet(atlases);
        profiler.popPush("baking");
        for (long it = this.topLevelModels_.beginIteration(); this.topLevelModels_.hasNextIteration(it); it = this.topLevelModels_.nextEntry(it)) {
            ResourceLocation resourceLocation = this.topLevelModels_.getIterationKey(it);
            BakedModel bakedModel = null;
            try {
                bakedModel = this.bake(resourceLocation, BlockModelRotation.X0_Y0);
            }
            catch (Exception e) {
                LOGGER.warn("Unable to bake model: '{}': {}", resourceLocation, e);
            }
            if (bakedModel != null) {
                this.bakedTopLevelModels_.put(resourceLocation, bakedModel);
            }
        }
        profiler.pop();
        return this.atlasSet;
    }

    @Shadow
    protected abstract BlockModel loadBlockModel(ResourceLocation resourceLocation) throws IOException;

    @Shadow
    protected abstract void registerModelGroup(Iterable<BlockState> iterable);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void cacheAndQueueDependencies(ResourceLocation resourceLocation, UnbakedModel unbakedModel) {
        this.unbakedCache_.put(resourceLocation, unbakedModel);
        this.loadingStack_.addAll(unbakedModel.getDependencies());
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void loadModel(ResourceLocation resLoc) throws Exception {
        if (!(resLoc instanceof ModelResourceLocation modelResourceLocation)) {
            this.cacheAndQueueDependencies(resLoc, this.loadBlockModel(resLoc));
            return;
        }
        if (Objects.equals(modelResourceLocation.getVariant(), "inventory")) {
            ResourceLocation itemLocation = new ResourceLocation(resLoc.getNamespace(), "item/" + resLoc.getPath());
            BlockModel blockModel = this.loadBlockModel(itemLocation);
            this.cacheAndQueueDependencies(modelResourceLocation, blockModel);
            this.unbakedCache_.put(itemLocation, blockModel);
            return;
        }
        ResourceLocation resourceLocation2 = new ResourceLocation(resLoc.getNamespace(), resLoc.getPath());
        StateDefinition<Block, BlockState> sd = STATIC_DEFINITIONS.get(resourceLocation2);
        if (sd == null) {
            sd = Registry.BLOCK.get(resourceLocation2).getStateDefinition();
        }
        StateDefinition<Block, BlockState> stateDefinition = sd;
        this.context.setDefinition(stateDefinition);
        OList<Property<?>> list = new OArrayList<>(this.blockColors.getColoringProperties(stateDefinition.getOwner())).view();
        OList<BlockState> possibleStates = stateDefinition.getPossibleStates_();
        O2OMap<ModelResourceLocation, BlockState> map = new O2OHashMap<>();
        for (int i = 0, len = possibleStates.size(); i < len; ++i) {
            BlockState state = possibleStates.get(i);
            map.put(BlockModelShaper.stateToModelLocation(resourceLocation2, state), state);
        }
        O2OMap<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> map2 = new O2OHashMap<>();
        ResourceLocation blockStateLocation = new ResourceLocation(resLoc.getNamespace(), "blockstates/" + resLoc.getPath() + ".json");
        UnbakedModel unbakedModel = this.unbakedCache_.get(MISSING_MODEL_LOCATION);
        ModelBakery.ModelGroupKey modelGroupKey = new ModelBakery.ModelGroupKey(OList.of(unbakedModel), OList.of());
        Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair = Pair.of(unbakedModel, () -> modelGroupKey);
        boolean var25 = false;
        O2OMap<ModelBakery.ModelGroupKey, RSet<BlockState>> map5 = new O2OHashMap<>();
        try {
            OList<Pair<String, BlockModelDefinition>> list2;
            try {
                var25 = true;
                List<Resource> resources = this.resourceManager.getResources(blockStateLocation);
                list2 = new OArrayList<>();
                for (int i = 0, len = resources.size(); i < len; ++i) {
                    Resource resource = resources.get(i);
                    try {
                        InputStream inputStream = resource.getInputStream();
                        Pair<String, BlockModelDefinition> var3;
                        try {
                            var3 = Pair.of(resource.getSourceName(), BlockModelDefinition.fromStream(this.context, new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
                        }
                        catch (Throwable e) {
                            //noinspection ConstantValue
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                }
                                catch (Throwable t) {
                                    e.addSuppressed(t);
                                }
                            }
                            throw e;
                        }
                        //noinspection ConstantValue
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        list2.add(var3);
                    }
                    catch (Exception e) {
                        throw new ModelBakery.BlockStateDefinitionException(String.format("Exception loading blockstate definition: '%s' in resourcepack: '%s': %s", resource.getLocation(), resource.getSourceName(), e.getMessage()));
                    }
                }
            }
            catch (IOException e) {
                LOGGER.warn("Exception loading blockstate definition: {}: {}", blockStateLocation, e);
                var25 = false;
                for (long it = map.beginIteration(); map.hasNextIteration(it); it = map.nextEntry(it)) {
                    ModelResourceLocation modelResLoc = map.getIterationKey(it);
                    BlockState blockState = map.getIterationValue(it);
                    Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair2 = map2.get(blockState);
                    if (pair2 == null) {
                        LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", blockStateLocation, modelResourceLocation);
                        pair2 = pair;
                    }
                    this.cacheAndQueueDependencies(modelResourceLocation, pair2.getFirst());
                    try {
                        ModelBakery.ModelGroupKey groupKey = pair2.getSecond().get();
                        RSet<BlockState> set = map5.get(groupKey);
                        if (set == null) {
                            set = new RHashSet<>();
                            map5.put(groupKey, set);
                            set.add(blockState);
                        }
                    }
                    catch (Exception var9) {
                        LOGGER.warn("Exception evaluating model definition: '{}'", modelResourceLocation, var9);
                    }
                }
                for (long it = map5.beginIteration(); map5.hasNextIteration(it); it = map5.nextEntry(it)) {
                    RSet<BlockState> set = map5.getIterationValue(it);
                    for (long it2 = set.beginIteration(); set.hasNextIteration(it2); it2 = set.nextEntry(it2)) {
                        BlockState blockState = set.getIteration(it2);
                        if (blockState.getRenderShape() != RenderShape.MODEL) {
                            it = set.removeIteration(it);
                            this.modelGroups_.put(blockState, 0);
                        }
                    }
                    if (set.size() > 1) {
                        this.registerModelGroup(set);
                    }
                }
                return;
            }
            for (int i = 0, len = list2.size(); i < len; ++i) {
                Pair<String, BlockModelDefinition> o = list2.get(i);
                BlockModelDefinition blockModelDefinition = o.getSecond();
                R2OMap<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> map4 = new R2OHashMap<>();
                MultiPart multiPart;
                if (blockModelDefinition.isMultiPart()) {
                    multiPart = blockModelDefinition.getMultiPart();
                    possibleStates.forEach(blockState -> map4.put(blockState, Pair.of(multiPart, () -> ModelBakery.ModelGroupKey.create(blockState, multiPart, list))));
                }
                else {
                    multiPart = null;
                }
                blockModelDefinition.getVariants().forEach((string, multiVariant) -> {
                    try {
                        possibleStates.stream().filter(predicate(stateDefinition, string)).forEach(blockState -> {
                            Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair2 = map4.put(blockState, Pair.of(multiVariant, () -> ModelBakery.ModelGroupKey.create(blockState, multiVariant, list)));
                            if (pair2 != null && pair2.getFirst() != multiPart) {
                                map4.put(blockState, pair);
                                Optional<Map.Entry<String, MultiVariant>> var10002 = blockModelDefinition.getVariants().entrySet().stream().filter(entry -> entry.getValue() == pair2.getFirst()).findFirst();
                                throw new RuntimeException("Overlapping definition with: " + var10002.get().getKey());
                            }
                        });
                    }
                    catch (Exception var12) {
                        LOGGER.warn("Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}", blockStateLocation, o.getFirst(), string, var12.getMessage());
                    }
                });
                map2.putAll(map4);
            }
            var25 = false;
            O2OMap<ModelBakery.ModelGroupKey, OSet<BlockState>> map3 = new O2OHashMap<>();
            for (long it = map.beginIteration(); map.hasNextIteration(it); it = map.nextEntry(it)) {
                ModelResourceLocation modelResLoc = map.getIterationKey(it);
                BlockState blockState = map.getIterationValue(it);
                Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair2 = map2.get(blockState);
                if (pair2 == null) {
                    LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", blockStateLocation, modelResLoc);
                    pair2 = pair;
                }
                this.cacheAndQueueDependencies(modelResLoc, pair2.getFirst());
                try {
                    ModelBakery.ModelGroupKey groupKey = pair2.getSecond().get();
                    RSet<BlockState> set = map5.get(groupKey);
                    if (set == null) {
                        set = new RHashSet<>();
                        map5.put(groupKey, set);
                        set.add(blockState);
                    }
                }
                catch (Exception var9) {
                    LOGGER.warn("Exception evaluating model definition: '{}'", modelResLoc, var9);
                }
            }
            for (long it = map3.beginIteration(); map3.hasNextIteration(it); it = map3.nextEntry(it)) {
                OSet<BlockState> set = map3.getIterationValue(it);
                for (long it2 = set.beginIteration(); set.hasNextIteration(it2); it2 = set.nextEntry(it2)) {
                    BlockState blockState = set.getIteration(it2);
                    if (blockState.getRenderShape() != RenderShape.MODEL) {
                        it2 = set.removeIteration(it2);
                        this.modelGroups_.put(blockState, 0);
                    }
                }
                if (set.size() > 1) {
                    this.registerModelGroup(set);
                }
            }
        }
        catch (ModelBakery.BlockStateDefinitionException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ModelBakery.BlockStateDefinitionException(String.format("Exception loading blockstate definition: '%s': %s", blockStateLocation, e));
        }
        finally {
            if (var25) {
                O2OMap<ModelBakery.ModelGroupKey, OSet<BlockState>> map6 = new O2OHashMap<>();
                for (long it = map.beginIteration(); map.hasNextIteration(it); it = map.nextEntry(it)) {
                    ModelResourceLocation modelResLoc = map.getIterationKey(it);
                    BlockState blockState = map.getIterationValue(it);
                    Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair2 = map2.get(blockState);
                    if (pair2 == null) {
                        LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", blockStateLocation, modelResLoc);
                        pair2 = pair;
                    }
                    this.cacheAndQueueDependencies(modelResLoc, pair2.getFirst());
                    try {
                        ModelBakery.ModelGroupKey groupKey = pair2.getSecond().get();
                        RSet<BlockState> set = map5.get(groupKey);
                        if (set == null) {
                            set = new RHashSet<>();
                            map5.put(groupKey, set);
                            set.add(blockState);
                        }
                    }
                    catch (Exception e) {
                        LOGGER.warn("Exception evaluating model definition: '{}'", modelResLoc, e);
                    }
                }
                for (long it = map6.beginIteration(); map6.hasNextIteration(it); it = map6.nextEntry(it)) {
                    OSet<BlockState> set = map6.getIterationValue(it);
                    for (long it2 = set.beginIteration(); set.hasNextIteration(it2); it2 = set.nextEntry(it2)) {
                        BlockState blockState = set.getIteration(it2);
                        if (blockState.getRenderShape() != RenderShape.MODEL) {
                            it2 = set.removeIteration(it2);
                            this.modelGroups_.put(blockState, 0);
                        }
                    }
                    if (set.size() > 1) {
                        this.registerModelGroup(set);
                    }
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void loadTopLevel(ModelResourceLocation modelResourceLocation) {
        UnbakedModel unbakedModel = this.getModel(modelResourceLocation);
        this.unbakedCache_.put(modelResourceLocation, unbakedModel);
        this.topLevelModels_.put(modelResourceLocation, unbakedModel);
    }
}
