package tgw.evolution.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import tgw.evolution.util.collection.lists.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class NBTHelper {

    private static final CompoundTag EMPTY_COMPOUND = new CompoundTag();

    private NBTHelper() {
    }

    public static boolean asBoolean(Tag input, String key, boolean defaultEntry) {
        return asByte(input, key, defaultEntry ? 1 : 0) == 1;
    }

    public static byte asByte(Tag input, String key, int defaultEntry) {
        return asNumber(input, key, defaultEntry).byteValue();
    }

    public static float asFloat(Tag input, String key, float defaultEntry) {
        return asNumber(input, key, defaultEntry).floatValue();
    }

    public static int asInt(Tag input, String key, int defaultEntry) {
        return asNumber(input, key, defaultEntry).intValue();
    }

    public static <U> List<U> asList(Tag nbt, String key, Function<Tag, U> deserializer) {
        Optional<Tag> optional = get(nbt, key);
        if (optional.isPresent()) {
            return asListOpt(optional.get(), deserializer).orElseGet(ImmutableList::of);
        }
        return ImmutableList.of();
    }

    public static <U> Optional<List<U>> asListOpt(Tag nbt, Function<Tag, U> deserializer) {
        return getStream(nbt).map(stream -> stream.map(deserializer).collect(Collectors.toList()));
    }

    public static Number asNumber(Tag input, String key, Number defaultEntry) {
        Optional<Tag> optional = get(input, key);
        if (optional.isPresent()) {
            return getNumberValue(optional.get()).orElse(defaultEntry);
        }
        return defaultEntry;
    }

    public static short asShort(Tag input, String key, int defaultEntry) {
        return asNumber(input, key, defaultEntry).shortValue();
    }

    public static String asString(Tag input, String key, String defaultEntry) {
        Optional<Tag> optional = get(input, key);
        if (optional.isPresent()) {
            return getStringValue(optional.get()).orElse(defaultEntry);
        }
        return defaultEntry;
    }

    public static Tag createList(Stream<Tag> list) {
        PeekingIterator<Tag> peekingiterator = Iterators.peekingIterator(list.iterator());
        if (!peekingiterator.hasNext()) {
            return new ListTag();
        }
        Tag tag = peekingiterator.peek();
        if (tag instanceof ByteTag byteTag) {
            BList bytes = new BArrayList(Iterators.transform(peekingiterator, byteNbt -> byteTag.getAsByte()));
            return new ByteArrayTag(bytes);
        }
        if (tag instanceof IntTag intTag) {
            IList integers = new IArrayList(Iterators.transform(peekingiterator, intNbt -> intTag.getAsInt()));
            return new IntArrayTag(integers);
        }
        if (tag instanceof LongTag longTag) {
            LList longs = new LArrayList(Iterators.transform(peekingiterator, longNbt -> longTag.getAsLong()));
            return new LongArrayTag(longs);
        }
        ListTag listnbt = new ListTag();
        while (peekingiterator.hasNext()) {
            Tag inbt1 = peekingiterator.next();
            if (!(inbt1 instanceof EndTag)) {
                listnbt.add(inbt1);
            }
        }
        return listnbt;
    }

    public static Tag createMap(Map<Tag, Tag> map) {
        CompoundTag compoundNBT = new CompoundTag();
        for (Map.Entry<Tag, Tag> entry : map.entrySet()) {
            compoundNBT.put(entry.getKey().getAsString(), entry.getValue());
        }
        return compoundNBT;
    }

    public static @Nullable ResourceLocation decodeResLoc(@Nullable Tag tag, Logger logger) {
        String s = decodeString(tag, logger);
        if (s == null) {
            return null;
        }
        ResourceLocation location = ResourceLocation.tryParse(s);
        if (location == null) {
            logger.error("Not a valid resource location: {}", s);
            return null;
        }
        return location;
    }

    public static <T> ResourceKey<T> decodeResourceKey(ResourceKey<Registry<T>> registry,
                                                       @Nullable Tag tag,
                                                       Logger logger,
                                                       ResourceKey<T> orElse) {
        ResourceKey<T> r = decodeResourceKey(registry, tag, logger);
        if (r == null) {
            return orElse;
        }
        return r;
    }

    public static <T> @Nullable ResourceKey<T> decodeResourceKey(ResourceKey<Registry<T>> registry, @Nullable Tag tag, Logger logger) {
        ResourceLocation r = decodeResLoc(tag, logger);
        if (r == null) {
            return null;
        }
        return ResourceKey.elementKey(registry).apply(r);
    }

    public static @Nullable String decodeString(@Nullable Tag tag, Logger logger) {
        if (tag == null) {
            return null;
        }
        if (tag.getId() != Tag.TAG_STRING) {
            logger.error("Tag {} is not a string!", tag);
            return null;
        }
        return tag.getAsString();
    }

    public static Tag emptyMap() {
        return createMap(ImmutableMap.of());
    }

    public static StringTag encode(ResourceLocation loc) {
        return StringTag.valueOf(loc.toString());
    }

    public static Optional<Tag> get(Tag input, String key) {
        return getGeneric(input, StringTag.valueOf(key));
    }

    public static @Nullable CompoundTag getCompound(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_COMPOUND) {
            return (CompoundTag) t;
        }
        return null;
    }

    /**
     * Use for reading only, do not write into this tag.
     */
    public static CompoundTag getCompoundOrEmpty(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_COMPOUND)) {
            //noinspection ConstantConditions
            return (CompoundTag) tag.get(key);
        }
        return EMPTY_COMPOUND;
    }

    /**
     * Can be safely used for writing. If the compound doesn't exist, it will be created and added into the original tag automatically.
     */
    public static CompoundTag getCompoundOrWrite(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_COMPOUND)) {
            //noinspection ConstantConditions
            return (CompoundTag) tag.get(key);
        }
        CompoundTag t = new CompoundTag();
        tag.put(key, t);
        return t;
    }

    public static Optional<Tag> getGeneric(Tag input, Tag key) {
        return getMapValues(input).flatMap(map -> Optional.ofNullable(map.get(key)));
    }

    public static int getInt(CompoundTag nbt, String key) {
        Tag tag = nbt.get(key);
        if (tag instanceof NumericTag t) {
            return t.getAsInt();
        }
        return 0;
    }

    public static Optional<Map<Tag, Tag>> getMapValues(Tag nbt) {
        if (nbt instanceof CompoundTag compoundTag) {
            return Optional.of(compoundTag.getAllKeys()
                                          .stream()
                                          .map(string -> Pair.of(StringTag.valueOf(string), compoundTag.get(string)))
                                          .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        }
        return Optional.empty();
    }

    public static Optional<Number> getNumberValue(Tag nbt) {
        return nbt instanceof NumericTag numericTag ? Optional.of(numericTag.getAsNumber()) : Optional.empty();
    }

    public static Optional<Stream<Tag>> getStream(Tag nbt) {
        return nbt instanceof CollectionTag collectionTag ? Optional.of(collectionTag.stream()) : Optional.empty();
    }

    public static Optional<String> getStringValue(Tag input) {
        return input instanceof StringTag ? Optional.of(input.getAsString()) : Optional.empty();
    }

    public static Tag mergeInto(Tag nbt1, Tag nbt2, Tag nbt3) {
        CompoundTag compound;
        if (nbt1 instanceof EndTag) {
            compound = new CompoundTag();
        }
        else {
            if (!(nbt1 instanceof CompoundTag comp)) {
                return nbt1;
            }
            compound = new CompoundTag();
            for (String string : comp.getAllKeys()) {
                compound.put(string, comp.get(string));
            }
        }
        compound.put(nbt2.getAsString(), nbt3);
        return compound;
    }

    public static Tag mergeInto(Tag nbt1, Tag nbt2) {
        if (nbt2 instanceof EndTag) {
            return nbt1;
        }
        if (!(nbt1 instanceof CompoundTag compound1)) {
            if (nbt1 instanceof EndTag) {
                throw new IllegalArgumentException("mergeInto called with a null input.");
            }
            if (nbt1 instanceof CollectionTag<?> collection) {
                CollectionTag<Tag> list = new ListTag();
                list.addAll(collection);
                list.add(nbt2);
                return list;
            }
            return nbt1;
        }
        if (!(nbt2 instanceof CompoundTag compound2)) {
            return nbt1;
        }
        CompoundTag compound = new CompoundTag();
        for (String s : compound1.getAllKeys()) {
            compound.put(s, compound1.get(s));
        }
        for (String s1 : compound2.getAllKeys()) {
            compound.put(s1, compound2.get(s1));
        }
        return compound;
    }

    public static NonNullList<ItemStack> readStackList(CompoundTag nbt) {
        NonNullList<ItemStack> list = NonNullList.withSize(getInt(nbt, "Size"), ItemStack.EMPTY);
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = getInt(itemTags, "Slot");
            if (slot >= 0 && slot < list.size()) {
                list.set(slot, ItemStack.of(itemTags));
            }
        }
        return list;
    }

    public static CompoundTag writeStackList(NonNullList<ItemStack> stacks) {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                //noinspection ObjectAllocationInLoop
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stacks.get(i).save(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }
}
