package tgw.evolution.blocks.util;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.world.level.block.state.properties.Property;
import tgw.evolution.util.collection.IOpenHashSet;
import tgw.evolution.util.collection.ISet;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("EqualsAndHashcode")
public class IntProperty extends Property<Integer> {

    protected final int max;
    protected final int min;
    private final IntSet values;

    protected IntProperty(String name, int min, int max) {
        super(name, Integer.class);
        if (min < 0) {
            throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater");
        }
        if (max <= min) {
            throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
        }
        this.min = min;
        this.max = max;
        ISet set = new IOpenHashSet();
        for (int i = min; i <= max; ++i) {
            set.add(i);
        }
        set.trimCollection();
        this.values = IntSets.unmodifiable(set);
    }

    public static IntProperty create(String name, int min, int max) {
        return new IntProperty(name, min, max);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof IntProperty i && super.equals(o)) {
            return i.min == this.min && i.max == this.max;
        }
        return false;
    }

    @Override
    public int generateHashCode() {
        return 31 * super.generateHashCode() + this.values.hashCode();
    }

    public int getMaxValue() {
        return this.max;
    }

    public int getMinValue() {
        return this.min;
    }

    /**
     * @return the name for the given value.
     */
    @Override
    public String getName(Integer value) {
        return value.toString();
    }

    @Override
    public Collection<Integer> getPossibleValues() {
        return this.values;
    }

    @Override
    public Optional<Integer> getValue(String value) {
        try {
            int integer = Integer.parseInt(value);
            return this.values.contains(integer) ? Optional.of(integer) : Optional.empty();
        }
        catch (NumberFormatException numberformatexception) {
            return Optional.empty();
        }
    }
}
