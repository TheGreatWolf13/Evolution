package tgw.evolution.mixin;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.patches.PatchIntegerProperty;
import tgw.evolution.util.collection.sets.IHashSet;
import tgw.evolution.util.collection.sets.ISet;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("EqualsAndHashcode")
@Mixin(IntegerProperty.class)
public abstract class Mixin_CF_IntegerProperty extends Property<Integer> implements PatchIntegerProperty {

    @Unique private final int max;
    @Unique private final int min;
    @Shadow @Final @DeleteField private ImmutableSet<Integer> values;
    @Unique private final ISet values_;

    @ModifyConstructor
    public Mixin_CF_IntegerProperty(String name, int min, int max) {
        super(name, Integer.class);
        if (min < 0) {
            throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater: " + min);
        }
        if (max <= min) {
            throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + "): " + max);
        }
        this.min = min;
        this.max = max;
        ISet set = new IHashSet();
        for (int k = min; k <= max; ++k) {
            set.add(k);
        }
        set.trim();
        this.values_ = set.view();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        //noinspection EqualsBetweenInconvertibleTypes
        if (object instanceof IntegerProperty integerProperty && super.equals(object)) {
            return this.min == integerProperty.minValue() && this.max == integerProperty.maxValue();
        }
        return false;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public int generateHashCode() {
        return 31 * (31 * super.generateHashCode() + this.min) + this.max;
    }

    @Override
    public String getName(int value) {
        return String.valueOf(value);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public String getName(Integer integer) {
        return this.getName(integer.intValue());
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Collection<Integer> getPossibleValues() {
        return this.values_;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Optional<Integer> getValue(String name) {
        Evolution.deprecatedMethod();
        return Optional.ofNullable(this.getValue_(name));
    }

    @Override
    public @Nullable Integer getValue_(String name) {
        try {
            int integer = Integer.parseInt(name);
            return this.values_.contains(integer) ? integer : null;
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public int maxValue() {
        return this.max;
    }

    @Override
    public int minValue() {
        return this.min;
    }

    @Override
    public ISet values() {
        return this.values_;
    }
}
