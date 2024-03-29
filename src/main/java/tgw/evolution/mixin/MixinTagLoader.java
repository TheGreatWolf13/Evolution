package tgw.evolution.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchEither;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mixin(TagLoader.class)
public abstract class MixinTagLoader<T> {

    @Shadow @Final private static Gson GSON;
    @Shadow @Final private static int PATH_SUFFIX_LENGTH;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private String directory;
    @Shadow @Final private Function<ResourceLocation, Optional<T>> idToValue;

    @Shadow
    private static void addDependencyIfNotCyclic(Multimap<ResourceLocation, ResourceLocation> multimap, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static void visitDependenciesAndElement(Map<ResourceLocation, Tag.Builder> map, Multimap<ResourceLocation, ResourceLocation> multimap, Set<ResourceLocation> set, ResourceLocation resourceLocation, BiConsumer<ResourceLocation, Tag.Builder> biConsumer) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Map<ResourceLocation, Tag<T>> build(Map<ResourceLocation, Tag.Builder> map) {
        O2OMap<ResourceLocation, Tag.Builder> builderMap = (O2OMap<ResourceLocation, Tag.Builder>) map;
        O2OMap<ResourceLocation, Tag<T>> newMap = new O2OHashMap<>();
        Function<ResourceLocation, Tag<T>> tagGetter = newMap::get;
        Function<ResourceLocation, T> itemGetter = resLoc -> this.idToValue.apply(resLoc).orElse(null);
        Multimap<ResourceLocation, ResourceLocation> multimap = HashMultimap.create();
        for (long it = builderMap.beginIteration(); builderMap.hasNextIteration(it); it = builderMap.nextEntry(it)) {
            ResourceLocation key = builderMap.getIterationKey(it);
            Tag.Builder builder = builderMap.getIterationValue(it);
            //noinspection ObjectAllocationInLoop
            builder.visitRequiredDependencies(resLoc -> addDependencyIfNotCyclic(multimap, key, resLoc));
        }
        for (long it = builderMap.beginIteration(); builderMap.hasNextIteration(it); it = builderMap.nextEntry(it)) {
            ResourceLocation key = builderMap.getIterationKey(it);
            Tag.Builder builder = builderMap.getIterationValue(it);
            //noinspection ObjectAllocationInLoop
            builder.visitOptionalDependencies(resLoc -> addDependencyIfNotCyclic(multimap, key, resLoc));
        }
        OSet<ResourceLocation> set = new OHashSet<>();
        for (long it = builderMap.beginIteration(); builderMap.hasNextIteration(it); it = builderMap.nextEntry(it)) {
            ResourceLocation key = builderMap.getIterationKey(it);
            visitDependenciesAndElement(builderMap, multimap, set, key, (resLoc, builder) -> {
                PatchEither<Collection<Tag.BuilderEntry>, Tag<T>> build = (PatchEither<Collection<Tag.BuilderEntry>, Tag<T>>) builder.build(tagGetter, itemGetter);
                if (build.isLeft()) {
                    LOGGER.error("Couldn't load tag {} as it is missing following references: {}", resLoc, build.getLeft().stream().map(Objects::toString).collect(Collectors.joining(", ")));
                }
                else {
                    newMap.put(resLoc, build.getRight());
                }
            });
        }
        return newMap;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Map<ResourceLocation, Tag.Builder> load(ResourceManager resourceManager) {
        Map<ResourceLocation, Tag.Builder> map = new O2OHashMap<>();
        for (ResourceLocation resourceLocation : resourceManager.listResources(this.directory, stringx -> stringx.endsWith(".json"))) {
            String string = resourceLocation.getPath();
            ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), string.substring(this.directory.length() + 1, string.length() - PATH_SUFFIX_LENGTH));
            try {
                List<Resource> resources = resourceManager.getResources(resourceLocation);
                for (int i = 0, len = resources.size(); i < len; ++i) {
                    Resource resource = resources.get(i);
                    try {
                        InputStream inputStream = resource.getInputStream();
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                            try {
                                JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                                if (jsonObject == null) {
                                    Evolution.error("Couldn't load tag list {} from {} in data pack {} as it is empty or null", resourceLocation2, resourceLocation, resource.getSourceName());
                                }
                                else {
                                    map.computeIfAbsent(resourceLocation2, resourceLocationx -> Tag.Builder.tag()).addFromJson(jsonObject, resource.getSourceName());
                                }
                            }
                            catch (Throwable e) {
                                try {
                                    reader.close();
                                }
                                catch (Throwable t) {
                                    e.addSuppressed(t);
                                }
                                throw e;
                            }
                            reader.close();
                        }
                        catch (Throwable e) {
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
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                    catch (RuntimeException | IOException e) {
                        Evolution.error("Couldn't read tag list {} from {} in data pack {}", resourceLocation2, resourceLocation, resource.getSourceName(), e);
                    }
                    finally {
                        IOUtils.closeQuietly(resource);
                    }
                }
            }
            catch (IOException e) {
                Evolution.error("Couldn't read tag list {} from {}", resourceLocation2, resourceLocation, e);
            }
        }
        return map;
    }
}
