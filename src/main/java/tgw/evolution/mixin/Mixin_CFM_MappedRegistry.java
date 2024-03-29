package tgw.evolution.mixin;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.*;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.*;
import tgw.evolution.util.collection.sets.OSet;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Mixin(MappedRegistry.class)
public abstract class Mixin_CFM_MappedRegistry<T> extends WritableRegistry<T> {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final @DeleteField private ObjectList<Holder.Reference<T>> byId;
    @Unique private final OList<Holder.Reference<T>> byId_;
    @Shadow @Final @DeleteField private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Unique private final O2OMap<ResourceKey<T>, Holder.Reference<T>> byKey_;
    @Shadow @Final @DeleteField private Map<ResourceLocation, Holder.Reference<T>> byLocation;
    @Unique private final O2OMap<ResourceLocation, Holder.Reference<T>> byLocation_;
    @Shadow @Final @DeleteField private Map<T, Holder.Reference<T>> byValue;
    @Unique private final R2OMap<T, Holder.Reference<T>> byValue_;
    @Mutable @Shadow @Final @RestoreFinal private @Nullable Function<T, Holder.Reference<T>> customHolderProvider;
    @Shadow private Lifecycle elementsLifecycle;
    @Shadow private boolean frozen;
    @Shadow @DeleteField private @Nullable List<Holder.Reference<T>> holdersInOrder;
    @Unique private @Nullable OList<Holder.Reference<T>> holdersInOrder_;
    @Shadow @DeleteField private @Nullable Map<T, Holder.Reference<T>> intrusiveHolderCache;
    @Unique private @Nullable R2OMap<T, Holder.Reference<T>> intrusiveHolderCache_;
    @Shadow @Final @DeleteField private Map<T, Lifecycle> lifecycles;
    @Unique private final R2OMap<T, Lifecycle> lifecycles_;
    @Shadow private int nextId;
    @Shadow @DeleteField private volatile Map<TagKey<T>, HolderSet.Named<T>> tags;
    @Unique private volatile R2OMap<TagKey<T>, HolderSet.Named<T>> tags_;
    @Shadow @Final @DeleteField private Object2IntMap<T> toId;
    @Unique private final R2IMap<T> toId_;

    @DummyConstructor
    public Mixin_CFM_MappedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, OList<Holder.Reference<T>> byId_, O2OMap<ResourceKey<T>, Holder.Reference<T>> byKey_, O2OMap<ResourceLocation, Holder.Reference<T>> byLocation_, R2OMap<T, Holder.Reference<T>> byValue_, R2OMap<T, Lifecycle> lifecycles_, R2IMap<T> toId_) {
        super(resourceKey, lifecycle);
        this.byId_ = byId_;
        this.byKey_ = byKey_;
        this.byLocation_ = byLocation_;
        this.byValue_ = byValue_;
        this.lifecycles_ = lifecycles_;
        this.toId_ = toId_;
    }

    @ModifyConstructor
    public Mixin_CFM_MappedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, @Nullable Function<T, Holder.Reference<T>> function) {
        super(resourceKey, lifecycle);
        this.byId_ = new OArrayList<>(256);
        R2IMap<T> toId = new R2IHashMap<>();
        toId.defaultReturnValue(-1);
        this.toId_ = toId;
        this.byLocation_ = new O2OHashMap<>();
        this.byKey_ = new O2OHashMap<>();
        this.byValue_ = new R2OHashMap<>();
        this.lifecycles_ = new R2OHashMap<>();
        this.tags_ = new R2OHashMap<>();
        this.elementsLifecycle = lifecycle;
        this.customHolderProvider = function;
        //noinspection VariableNotUsedInsideIf
        if (function != null) {
            this.intrusiveHolderCache_ = new R2OHashMap<>();
        }
    }

    @Shadow
    private static <T> @Nullable T getValueFromNullable(Holder.@Nullable Reference<T> reference) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static void method_39665(Object2IntOpenCustomHashMap par1) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static void method_40259(Map par1, Holder.Reference par2) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static String method_40563(TagKey par1) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void bindTags(Map<TagKey<T>, List<Holder<T>>> map) {
        R2OMap<Holder.Reference<T>, OList<TagKey<T>>> newMap = new R2OHashMap<>();
        O2OMap<ResourceKey<T>, Holder.Reference<T>> byKey = this.byKey_;
        for (long it = byKey.beginIteration(); byKey.hasNextIteration(it); it = byKey.nextEntry(it)) {
            //noinspection ObjectAllocationInLoop
            newMap.put(byKey.getIterationValue(it), new OArrayList<>());
        }
        O2OMap<TagKey<T>, List<Holder<T>>> map1 = (O2OMap<TagKey<T>, List<Holder<T>>>) map;
        for (long it = map1.beginIteration(); map1.hasNextIteration(it); it = map1.nextEntry(it)) {
            TagKey<T> tagKey = map1.getIterationKey(it);
            List<Holder<T>> list = map1.getIterationValue(it);
            for (int i = 0, len = list.size(); i < len; ++i) {
                Holder<T> tHolder = list.get(i);
                if (!tHolder.isValidInRegistry(this)) {
                    throw new IllegalStateException("Can't create named set " + tagKey + " containing value " + tHolder + " from outside registry " + this);
                }
                if (!(tHolder instanceof Holder.Reference ref)) {
                    throw new IllegalStateException("Found direct holder " + tHolder + " value in tag " + tagKey);
                }
                newMap.get(ref).add(tagKey);
            }
        }
        R2OMap<TagKey<T>, HolderSet.Named<T>> tags = this.tags_;
        for (long it = tags.beginIteration(); tags.hasNextIteration(it); it = tags.nextEntry(it)) {
            TagKey<T> tagKey = tags.getIterationKey(it);
            if (tagKey != null && !map.containsKey(tagKey)) {
                LOGGER.warn("Not all defined tags for registry {} are present in data pack: {}", this.key(), tagKey.location());
            }
        }
        R2OMap<TagKey<T>, HolderSet.Named<T>> map3 = new R2OHashMap<>(this.tags_);
        for (long it = map1.beginIteration(); map1.hasNextIteration(it); it = map1.nextEntry(it)) {
            TagKey<T> tagKey = map1.getIterationKey(it);
            List<Holder<T>> list = map1.getIterationValue(it);
            HolderSet.Named<T> holders = map3.get(tagKey);
            if (holders == null) {
                holders = this.createTag(tagKey);
                map3.put(tagKey, holders);
            }
            holders.bind(list);
        }
        for (long it = newMap.beginIteration(); newMap.hasNextIteration(it); it = newMap.nextEntry(it)) {
            Holder.Reference<T> key = newMap.getIterationKey(it);
            OList<TagKey<T>> value = newMap.getIterationValue(it);
            assert key != null;
            key.bindTags(value);
        }
        this.tags_ = map3;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public @Nullable T byId(int i) {
        return i >= 0 && i < this.byId_.size() ? getValueFromNullable(this.byId_.get(i)) : null;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean containsKey(ResourceLocation resourceLocation) {
        return this.byLocation_.containsKey(resourceLocation);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean containsKey(ResourceKey<T> resourceKey) {
        return this.byKey_.containsKey(resourceKey);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Holder.Reference<T> createIntrusiveHolder(T object) {
        if (this.customHolderProvider == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        }
        if (!this.frozen && this.intrusiveHolderCache_ != null) {
            Holder.Reference<T> reference = this.intrusiveHolderCache_.get(object);
            if (reference == null) {
                reference = Holder.Reference.createIntrusive(this, object);
                this.intrusiveHolderCache_.put(object, reference);
            }
            return reference;
        }
        throw new IllegalStateException("Registry is already frozen");
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableSet(Maps.transformValues(this.byKey_, Holder::value).entrySet());
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Registry<T> freeze() {
        this.frozen = true;
        O2OMap<ResourceKey<T>, Holder.Reference<T>> byKey = this.byKey_;
        for (long it = byKey.beginIteration(); byKey.hasNextIteration(it); it = byKey.nextEntry(it)) {
            Holder.Reference<T> value = byKey.getIterationValue(it);
            if (!value.isBound()) {
                throw new IllegalStateException("Unbound values in registry " + this.key() + ": " + byKey.getIterationKey(it).location());
            }
        }
        R2OMap<T, Holder.Reference<T>> intrusiveHolderCache = this.intrusiveHolderCache_;
        if (intrusiveHolderCache != null) {
            for (long it = intrusiveHolderCache.beginIteration(); intrusiveHolderCache.hasNextIteration(it); it = intrusiveHolderCache.nextEntry(it)) {
                Holder.Reference<T> reference = intrusiveHolderCache.getIterationValue(it);
                if (!reference.isBound()) {
                    throw new IllegalStateException("Some intrusive holders were not added to registry: " + reference.key().location());
                }
            }
            this.intrusiveHolderCache_ = null;
        }
        return this;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public @Nullable T get(@Nullable ResourceLocation resourceLocation) {
        return getValueFromNullable(this.byLocation_.get(resourceLocation));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public @Nullable T get(@Nullable ResourceKey<T> resourceKey) {
        return getValueFromNullable(this.byKey_.get(resourceKey));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Optional<Holder<T>> getHolder(ResourceKey<T> resourceKey) {
        return Optional.ofNullable(this.byKey_.get(resourceKey));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Optional<Holder<T>> getHolder(int i) {
        return i >= 0 && i < this.byId_.size() ? Optional.ofNullable(this.byId_.get(i)) : Optional.empty();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public int getId(@Nullable T object) {
        return this.toId_.getInt(object);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public @Nullable ResourceLocation getKey(T object) {
        Holder.Reference<T> reference = this.byValue_.get(object);
        return reference != null ? reference.key().location() : null;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Holder<T> getOrCreateHolder(ResourceKey<T> resourceKey) {
        Holder.Reference<T> reference = this.byKey_.get(resourceKey);
        if (reference == null) {
            //noinspection VariableNotUsedInsideIf
            if (this.customHolderProvider != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            }
            this.validateWrite(resourceKey);
            reference = Holder.Reference.createStandAlone(this, resourceKey);
            this.byKey_.put(resourceKey, reference);
        }
        return reference;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public HolderSet.Named<T> getOrCreateTag(TagKey<T> tagKey) {
        HolderSet.Named<T> named = this.tags_.get(tagKey);
        if (named == null) {
            named = this.createTag(tagKey);
            R2OMap<TagKey<T>, HolderSet.Named<T>> map = new R2OHashMap<>(this.tags_);
            map.put(tagKey, named);
            this.tags_ = map;
        }
        return named;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Optional<ResourceKey<T>> getResourceKey(T object) {
        Holder.Reference<T> reference = this.byValue_.get(object);
        if (reference == null) {
            return Optional.empty();
        }
        return Optional.of(reference.key());
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Optional<HolderSet.Named<T>> getTag(TagKey<T> tagKey) {
        return Optional.ofNullable(this.tags_.get(tagKey));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Stream<TagKey<T>> getTagNames() {
        return this.tags_.keySet().stream();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
        return this.tags_.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public final List<Holder.Reference<T>> holdersInOrder() {
        if (this.holdersInOrder_ == null) {
            OList<Holder.Reference<T>> list = new OArrayList<>();
            OList<Holder.Reference<T>> byId = this.byId_;
            for (int i = 0, len = byId.size(); i < len; ++i) {
                Holder.Reference<T> reference = byId.get(i);
                if (reference != null) {
                    list.add(reference);
                }
            }
            this.holdersInOrder_ = list;
        }
        return this.holdersInOrder_;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean isEmpty() {
        return this.byKey_.isEmpty();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean isKnownTagName(TagKey<T> tagKey) {
        return this.tags_.containsKey(tagKey);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.byLocation_.keySet());
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Lifecycle lifecycle(T object) {
        return this.lifecycles_.get(object);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Holder<T> registerOrOverride(OptionalInt optionalInt, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle) {
        this.validateWrite(resourceKey);
        Holder<T> holder = this.byKey_.get(resourceKey);
        T t = holder != null && holder.isBound() ? holder.value() : null;
        int i;
        if (t == null) {
            if (optionalInt.isPresent()) {
                i = optionalInt.getAsInt();
            }
            else {
                i = this.nextId;
            }
        }
        else {
            i = this.toId_.getInt(t);
            if (optionalInt.isPresent() && optionalInt.getAsInt() != i) {
                throw new IllegalStateException("ID mismatch");
            }
            this.lifecycles_.remove(t);
            this.toId_.removeInt(t);
            this.byValue_.remove(t);
        }
        return this.registerMapping(i, resourceKey, object, lifecycle, false);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void resetTags() {
        R2OMap<TagKey<T>, HolderSet.Named<T>> tags = this.tags_;
        for (long it = tags.beginIteration(); tags.hasNextIteration(it); it = tags.nextEntry(it)) {
            tags.getIterationValue(it).bind(OList.emptyList());
        }
        O2OMap<ResourceKey<T>, Holder.Reference<T>> byKey = this.byKey_;
        for (long it = byKey.beginIteration(); byKey.hasNextIteration(it); it = byKey.nextEntry(it)) {
            byKey.getIterationValue(it).bindTags(OSet.emptySet());
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public int size() {
        return this.byKey_.size();
    }

    @Shadow
    protected abstract HolderSet.Named<T> createTag(TagKey<T> tagKey);

    @Shadow
    protected abstract void validateWrite(ResourceKey<T> resourceKey);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private void method_40258(Map par1, TagKey par2, List par3) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private void method_40263(Map par1, TagKey par2, List par3) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private Holder.Reference method_40275(ResourceKey par1) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private Holder<T> registerMapping(int i, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle, boolean bl) {
        this.validateWrite(resourceKey);
        this.byId_.size(Math.max(this.byId_.size(), i + 1));
        this.toId_.put(object, i);
        this.holdersInOrder_ = null;
        if (bl && this.byKey_.containsKey(resourceKey)) {
            Util.logAndPauseIfInIde("Adding duplicate key '" + resourceKey + "' to registry");
        }
        if (this.byValue_.containsKey(object)) {
            Util.logAndPauseIfInIde("Adding duplicate value '" + object + "' to registry");
        }
        this.lifecycles_.put(object, lifecycle);
        this.elementsLifecycle = this.elementsLifecycle.add(lifecycle);
        if (this.nextId <= i) {
            this.nextId = i + 1;
        }
        Holder.Reference reference;
        if (this.customHolderProvider != null) {
            reference = this.customHolderProvider.apply(object);
            Holder.Reference<T> oldReference = this.byKey_.put(resourceKey, reference);
            if (oldReference != null && oldReference != reference) {
                throw new IllegalStateException("Invalid holder present for key " + resourceKey);
            }
        }
        else {
            reference = this.byKey_.get(resourceKey);
            if (reference == null) {
                reference = Holder.Reference.createStandAlone(this, resourceKey);
                this.byKey_.put(resourceKey, reference);
            }
        }
        this.byLocation_.put(resourceKey.location(), reference);
        this.byValue_.put(object, reference);
        reference.bind(resourceKey, object);
        this.byId_.set(i, reference);
        return reference;
    }
}
