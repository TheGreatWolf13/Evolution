package tgw.evolution.util;

import com.google.common.collect.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.NonNullList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class NBTHelper {

    private NBTHelper() {
    }

    public static boolean asBoolean(INBT input, String key, boolean defaultEntry) {
        return asByte(input, key, defaultEntry ? 1 : 0) == 1;
    }

    public static byte asByte(INBT input, String key, int defaultEntry) {
        return asNumber(input, key, defaultEntry).byteValue();
    }

    public static float asFloat(INBT input, String key, float defaultEntry) {
        return asNumber(input, key, defaultEntry).floatValue();
    }

    public static int asInt(INBT input, String key, int defaultEntry) {
        return asNumber(input, key, defaultEntry).intValue();
    }

    public static <U> List<U> asList(INBT nbt, String key, Function<INBT, U> deserializer) {
        Optional<INBT> optional = get(nbt, key);
        if (optional.isPresent()) {
            return asListOpt(optional.get(), deserializer).orElseGet(ImmutableList::of);
        }
        return ImmutableList.of();
    }

    public static <U> Optional<List<U>> asListOpt(INBT nbt, Function<INBT, U> deserializer) {
        return getStream(nbt).map(stream -> stream.map(deserializer).collect(Collectors.toList()));
    }

    public static Number asNumber(INBT input, String key, Number defaultEntry) {
        Optional<INBT> optional = get(input, key);
        if (optional.isPresent()) {
            return getNumberValue(optional.get()).orElse(defaultEntry);
        }
        return defaultEntry;
    }

    public static short asShort(INBT input, String key, int defaultEntry) {
        return asNumber(input, key, defaultEntry).shortValue();
    }

    public static String asString(INBT input, String key, String defaultEntry) {
        Optional<INBT> optional = get(input, key);
        if (optional.isPresent()) {
            return getStringValue(optional.get()).orElse(defaultEntry);
        }
        return defaultEntry;
    }

    public static INBT createList(Stream<INBT> list) {
        PeekingIterator<INBT> peekingiterator = Iterators.peekingIterator(list.iterator());
        if (!peekingiterator.hasNext()) {
            return new ListNBT();
        }
        INBT inbt = peekingiterator.peek();
        if (inbt instanceof ByteNBT) {
            List<Byte> list2 = Lists.newArrayList(Iterators.transform(peekingiterator, byteNbt -> ((NumberNBT) byteNbt).getAsByte()));
            return new ByteArrayNBT(list2);
        }
        if (inbt instanceof IntNBT) {
            List<Integer> list1 = Lists.newArrayList(Iterators.transform(peekingiterator, intNbt -> ((NumberNBT) intNbt).getAsInt()));
            return new IntArrayNBT(list1);
        }
        if (inbt instanceof LongNBT) {
            List<Long> longList = Lists.newArrayList(Iterators.transform(peekingiterator, longNbt -> ((NumberNBT) longNbt).getAsLong()));
            return new LongArrayNBT(longList);
        }
        ListNBT listnbt = new ListNBT();
        while (peekingiterator.hasNext()) {
            INBT inbt1 = peekingiterator.next();
            if (!(inbt1 instanceof EndNBT)) {
                listnbt.add(inbt1);
            }
        }
        return listnbt;
    }

    public static INBT createMap(Map<INBT, INBT> map) {
        CompoundNBT compoundNBT = new CompoundNBT();
        for (Map.Entry<INBT, INBT> entry : map.entrySet()) {
            compoundNBT.put(entry.getKey().getAsString(), entry.getValue());
        }
        return compoundNBT;
    }

    public static INBT emptyMap() {
        return createMap(ImmutableMap.of());
    }

    public static Optional<INBT> get(INBT input, String key) {
        return getGeneric(input, StringNBT.valueOf(key));
    }

    public static Optional<INBT> getGeneric(INBT input, INBT key) {
        return getMapValues(input).flatMap(map -> Optional.ofNullable(map.get(key)));
    }

    public static Optional<Map<INBT, INBT>> getMapValues(INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            CompoundNBT compound = (CompoundNBT) nbt;
            return Optional.of(compound.getAllKeys()
                                       .stream()
                                       .map(string -> Pair.of(StringNBT.valueOf(string), compound.get(string)))
                                       .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        }
        return Optional.empty();
    }

    public static Optional<Number> getNumberValue(INBT nbt) {
        return nbt instanceof NumberNBT ? Optional.of(((NumberNBT) nbt).getAsNumber()) : Optional.empty();
    }

    public static Optional<Stream<INBT>> getStream(INBT nbt) {
        return nbt instanceof CollectionNBT ? Optional.of(((CollectionNBT<?>) nbt).stream().map(Function.identity())) : Optional.empty();
    }

    public static Optional<String> getStringValue(INBT input) {
        return input instanceof StringNBT ? Optional.of(input.getAsString()) : Optional.empty();
    }

    public static INBT mergeInto(INBT nbt1, INBT nbt2, INBT nbt3) {
        CompoundNBT compound;
        if (nbt1 instanceof EndNBT) {
            compound = new CompoundNBT();
        }
        else {
            if (!(nbt1 instanceof CompoundNBT)) {
                return nbt1;
            }
            CompoundNBT compoundnbt1 = (CompoundNBT) nbt1;
            compound = new CompoundNBT();
            compoundnbt1.getAllKeys().forEach(string -> compound.put(string, compoundnbt1.get(string)));
        }
        compound.put(nbt2.getAsString(), nbt3);
        return compound;
    }

    public static INBT mergeInto(INBT nbt1, INBT nbt2) {
        if (nbt2 instanceof EndNBT) {
            return nbt1;
        }
        if (!(nbt1 instanceof CompoundNBT)) {
            if (nbt1 instanceof EndNBT) {
                throw new IllegalArgumentException("mergeInto called with a null input.");
            }
            if (nbt1 instanceof CollectionNBT) {
                CollectionNBT<INBT> list = new ListNBT();
                CollectionNBT<?> collection = (CollectionNBT<?>) nbt1;
                list.addAll(collection);
                list.add(nbt2);
                return list;
            }
            return nbt1;
        }
        if (!(nbt2 instanceof CompoundNBT)) {
            return nbt1;
        }
        CompoundNBT compound = new CompoundNBT();
        CompoundNBT compound1 = (CompoundNBT) nbt1;
        for (String s : compound1.getAllKeys()) {
            compound.put(s, compound1.get(s));
        }
        CompoundNBT compound2 = (CompoundNBT) nbt2;
        for (String s1 : compound2.getAllKeys()) {
            compound.put(s1, compound2.get(s1));
        }
        return compound;
    }

    public static NonNullList<ItemStack> readStackList(CompoundNBT nbt) {
        NonNullList<ItemStack> list = NonNullList.withSize(nbt.getInt("Size"), ItemStack.EMPTY);
        ListNBT tagList = nbt.getList("Items", NBTTypes.COMPOUND_NBT);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");
            if (slot >= 0 && slot < list.size()) {
                list.set(slot, ItemStack.of(itemTags));
            }
        }
        return list;
    }

    public static CompoundNBT writeStackList(NonNullList<ItemStack> stacks) {
        ListNBT nbtTagList = new ListNBT();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                //noinspection ObjectAllocationInLoop
                CompoundNBT itemTag = new CompoundNBT();
                itemTag.putInt("Slot", i);
                stacks.get(i).save(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }
}
