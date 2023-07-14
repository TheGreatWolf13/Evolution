//package tgw.evolution.datagen;
//
//import com.google.common.base.Preconditions;
//import net.minecraft.data.DataGenerator;
//import net.minecraft.data.HashCache;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.packs.PackType;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.level.block.Block;
//import net.minecraftforge.client.model.generators.ModelBuilder;
//import net.minecraftforge.client.model.generators.ModelFile;
//import net.minecraftforge.common.data.ExistingFileHelper;
//import tgw.evolution.util.collection.maps.O2OHashMap;
//import tgw.evolution.util.collection.maps.O2OMap;
//
//import java.nio.file.Path;
//import java.util.Collection;
//import java.util.function.BiFunction;
//import java.util.function.Function;
//
//public abstract class ModelProvider<T extends ModelBuilder<T>> implements EvolutionDataProvider<ResourceLocation> {
//
//    public static final ExistingFileHelper.ResourceType TEXTURE = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png",
//    "textures");
//    protected static final String BLOCK_FOLDER = "block";
//    protected static final String ITEM_FOLDER = "item";
//    protected static final ExistingFileHelper.ResourceType MODEL = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".json",
//    "models");
//    protected final Function<ResourceLocation, T> factory;
//    protected final String folder;
//    protected final O2OMap<ResourceLocation, T> generatedModels = new O2OHashMap<>();
//    protected final DataGenerator generator;
//    protected final String modId;
//    private final ExistingFileHelper existingFileHelper;
//    private final Collection<Path> existingPaths;
//
//    public ModelProvider(GenBundle bundle, String modId, String folder, Function<ResourceLocation, T> factory) {
//        this.generator = bundle.generator();
//        this.existingPaths = bundle.existingPaths();
//        assert bundle.existingFileHelper() != null;
//        this.existingFileHelper = bundle.existingFileHelper();
//        this.modId = modId;
//        this.folder = folder;
//        this.factory = factory;
//    }
//
//    public ModelProvider(GenBundle bundle, String modid, String folder, BiFunction<ResourceLocation, ExistingFileHelper, T> builderFromModId) {
//        this(bundle, modid, folder, loc -> builderFromModId.apply(loc, bundle.existingFileHelper()));
//    }
//
//    protected static ResourceLocation blockTexture(Block block, String post) {
//        ResourceLocation name = block.getRegistryName();
//        assert name != null;
//        return new ResourceLocation(name.getNamespace(), "block/" + name.getPath() + post);
//    }
//
//    protected static ResourceLocation blockTexture(Block block) {
//        ResourceLocation name = block.getRegistryName();
//        assert name != null;
//        return new ResourceLocation(name.getNamespace(), "block/" + name.getPath());
//    }
//
//    protected static String name(Block block, String post) {
//        return name(block) + post;
//    }
//
//    protected static String name(Item block, String post) {
//        return name(block) + post;
//    }
//
//    protected static String name(Item block) {
//        //noinspection ConstantConditions
//        return block.getRegistryName().getPath();
//    }
//
//    protected static String name(Block block) {
//        //noinspection ConstantConditions
//        return block.getRegistryName().getPath();
//    }
//
//    public T builtin(String name, ResourceLocation particle) {
//        return this.getBuilder(name).texture("particle", particle);
//    }
//
//    protected void clear() {
//        this.generatedModels.clear();
//    }
//
//    public T cross(String name, ResourceLocation cross) {
//        return this.singleTexture(name, BLOCK_FOLDER + "/cross", "cross", cross);
//    }
//
//    public T cubeAll(String name, ResourceLocation texture) {
//        return this.singleTexture(name, BLOCK_FOLDER + "/cube_all", "all", texture);
//    }
//
//    public T cubeBottomTop(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
//        return this.sideBottomTop(name, BLOCK_FOLDER + "/cube_bottom_top", side, bottom, top);
//    }
//
//    public T cubeColumn(String name, ResourceLocation side, ResourceLocation end) {
//        return this.withExistingParent(name, BLOCK_FOLDER + "/cube_column").texture("side", side).texture("end", end);
//    }
//
//    @Override
//    public Collection<Path> existingPaths() {
//        return this.existingPaths;
//    }
//
//    private ResourceLocation extendWithFolder(ResourceLocation rl) {
//        if (rl.getPath().contains("/")) {
//            return rl;
//        }
//        return new ResourceLocation(rl.getNamespace(), this.folder + "/" + rl.getPath());
//    }
//
//    protected void generateAll(HashCache cache) {
//        for (T model : this.generatedModels.values()) {
//            Path path = this.getPath(model);
//            this.save(cache, model.toJson(), path, model.getLocation());
//        }
//    }
//
//    protected T getBuilder(String path) {
//        Preconditions.checkNotNull(path, "Path must not be null");
//        ResourceLocation outputLoc = this.extendWithFolder(path.contains(":") ? new ResourceLocation(path) : new ResourceLocation(this.modId,
//        path));
//        this.existingFileHelper.trackGenerated(outputLoc, MODEL);
//        return this.generatedModels.computeIfAbsent(outputLoc, this.factory);
//    }
//
//    public ModelFile.ExistingModelFile getExistingFile(ResourceLocation path) {
//        ModelFile.ExistingModelFile ret = new ModelFile.ExistingModelFile(this.extendWithFolder(path), this.existingFileHelper);
//        ret.assertExistence();
//        return ret;
//    }
//
//    private Path getPath(T model) {
//        return this.generator.getOutputFolder().resolve(this.makePath(model.getLocation()));
//    }
//
//    public T leaves(String name, ResourceLocation texture) {
//        return this.singleTexture(name, BLOCK_FOLDER + "/leaves", "all", texture);
//    }
//
//    @Override
//    public String makePath(ResourceLocation id) {
//        return "assets/" + id.getNamespace() + "/models/" + id.getPath() + ".json";
//    }
//
//    protected abstract void registerModels();
//
//    @Override
//    public void run(HashCache cache) {
//        this.clear();
//        this.registerModels();
//        this.generateAll(cache);
//    }
//
//    private T sideBottomTop(String name, String parent, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
//        return this.withExistingParent(name, parent)
//                   .texture("side", side)
//                   .texture("bottom", bottom)
//                   .texture("top", top);
//    }
//
//    private T singleTexture(String name, String parent, String textureKey, ResourceLocation texture) {
//        return this.singleTexture(name, new ResourceLocation(parent), textureKey, texture);
//    }
//
//    public T singleTexture(String name, ResourceLocation parent, String textureKey, ResourceLocation texture) {
//        return this.withExistingParent(name, parent).texture(textureKey, texture);
//    }
//
//    public T slab(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
//        return this.sideBottomTop(name, BLOCK_FOLDER + "/slab", side, bottom, top);
//    }
//
//    public T withExistingParent(String name, String parent) {
//        return this.withExistingParent(name, new ResourceLocation(parent));
//    }
//
//    protected T withExistingParent(String name, ResourceLocation parent) {
//        return this.getBuilder(name).parent(this.getExistingFile(parent));
//    }
//}
