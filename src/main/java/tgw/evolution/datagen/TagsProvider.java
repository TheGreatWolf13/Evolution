package tgw.evolution.datagen;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import tgw.evolution.datagen.util.ExistingFileHelper;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class TagsProvider<T> implements EvolutionDataProvider<ResourceLocation> {

    protected final Object2ObjectMap<ResourceLocation, Tag.Builder> builders = new Object2ObjectLinkedOpenHashMap<>();
    protected final ExistingFileHelper existingFileHelper;
    protected final Collection<Path> existingPaths;
    protected final DataGenerator generator;
    protected final String modId;
    protected final Registry<T> registry;
    private final OSet<TagKey<T>> addedTags = new OHashSet<>();
    private final List<TagKey<T>> allToRegister;
    private final ExistingFileHelper.ResourceType resourceType;

    protected TagsProvider(DataGenerator generator, Collection<Path> existingPaths, ExistingFileHelper existingFileHelper, Registry<T> registry, String modId, List<TagKey<T>> allToRegister) {
        this.generator = generator;
        this.registry = registry;
        this.existingFileHelper = existingFileHelper;
        this.modId = modId;
        this.existingPaths = existingPaths;
        this.resourceType = new ExistingFileHelper.ResourceType(PackType.SERVER_DATA, ".json", TagManager.getTagDir(registry.key()));
        this.allToRegister = allToRegister;
    }

    protected abstract void addTags();

    @Override
    public Collection<Path> existingPaths() {
        return this.existingPaths;
    }

    protected Tag.Builder getOrCreateRawBuilder(TagKey<T> tag) {
        this.addedTags.add(tag);
        Tag.Builder builder = this.builders.get(tag.location());
        if (builder == null) {
            this.existingFileHelper.trackGenerated(tag.location(), this.resourceType);
            builder = new Tag.Builder();
            this.builders.put(tag.location(), builder);
        }
        return builder;
    }

    protected Path getPath(ResourceLocation id) {
        return this.generator.getOutputFolder()
                             .resolve("data/" + id.getNamespace() + "/" + TagManager.getTagDir(this.registry.key()) + "/" + id.getPath() + ".json");
    }

    @Override
    public String makePath(ResourceLocation id) {
        return "data/" + id.getNamespace() + "/" + TagManager.getTagDir(this.registry.key()) + "/" + id.getPath() + ".json";
    }

    private boolean missing(Tag.BuilderEntry reference) {
        Tag.Entry entry = reference.entry();
        // We only care about non-optional tag entries, this is the only type that can reference a resource and needs validation
        // Optional tags should not be validated
        if (entry instanceof Tag.TagEntry nonOptionalEntry) {
            return !this.existingFileHelper.exists(nonOptionalEntry.id, this.resourceType);
        }
        return false;
    }

    @Override
    public void run(HashCache cache) throws IOException {
        this.builders.clear();
        this.addTags();
        for (int i = 0, len = this.allToRegister.size(); i < len; i++) {
            if (!this.addedTags.contains(this.allToRegister.get(i))) {
                throw new IllegalStateException(this.tagType() + " Tag " + this.allToRegister.get(i).location() + " has not been registered!");
            }
        }
        OList<Tag.BuilderEntry> list = new OArrayList<>();
        for (Map.Entry<ResourceLocation, Tag.Builder> entry : this.builders.entrySet()) {
            ResourceLocation key = entry.getKey();
            Tag.Builder value = entry.getValue();
            //noinspection ObjectAllocationInLoop
            value.getEntries().forEach(e -> {
                //noinspection ObjectAllocationInLoop
                if (!e.entry().verifyIfPresent(this.registry::containsKey, this.builders::containsKey)) {
                    if (this.missing(e)) {
                        list.add(e);
                    }
                }
            });
            if (!list.isEmpty()) {
                throw new IllegalArgumentException(String.format("Couldn't define tag %s as it is missing following references: %s", key,
                                                                 list.stream().map(Objects::toString).collect(Collectors.joining(","))));
            }
            JsonObject json = value.serializeToJson();
            Path path = this.getPath(key);
            this.save(cache, json, path, key);
        }
    }

    protected TagsProvider.TagAppender<T> tag(TagKey<T> tag) {
        Tag.Builder builder = this.getOrCreateRawBuilder(tag);
        return new TagsProvider.TagAppender<>(builder, this.registry, this.modId);
    }

    protected abstract String tagType();

    @Override
    public String type() {
        return this.tagType() + "Tag";
    }

    public static class TagAppender<T> {
        public final Registry<T> registry;
        private final Tag.Builder builder;
        private final String source;

        TagAppender(Tag.Builder builder, Registry<T> registry, String source) {
            this.builder = builder;
            this.registry = registry;
            this.source = source;
        }

        public TagsProvider.TagAppender<T> add(T item) {
            //noinspection ConstantConditions
            this.builder.addElement(this.registry.getKey(item), this.source);
            return this;
        }

        @SafeVarargs
        public final TagsProvider.TagAppender<T> add(ResourceKey<T>... toAdd) {
            for (ResourceKey<T> key : toAdd) {
                this.builder.addElement(key.location(), this.source);
            }
            return this;
        }

        @SafeVarargs
        public final TagsProvider.TagAppender<T> add(T... toAdd) {
            for (T t : toAdd) {
                //noinspection ConstantConditions
                this.builder.addElement(this.registry.getKey(t), this.source);
            }
            return this;
        }

        public TagsProvider.TagAppender<T> add(Tag.Entry tag) {
            this.builder.add(tag, this.source);
            return this;
        }

        public TagsProvider.TagAppender<T> addOptional(ResourceLocation pLocation) {
            this.builder.addOptionalElement(pLocation, this.source);
            return this;
        }

        public TagsProvider.TagAppender<T> addOptionalTag(ResourceLocation pLocation) {
            this.builder.addOptionalTag(pLocation, this.source);
            return this;
        }

        public TagsProvider.TagAppender<T> addTag(TagKey<T> pTag) {
            this.builder.addTag(pTag.location(), this.source);
            return this;
        }

        public Tag.Builder getInternalBuilder() {
            return this.builder;
        }

        public String getModID() {
            return this.source;
        }
    }
}
