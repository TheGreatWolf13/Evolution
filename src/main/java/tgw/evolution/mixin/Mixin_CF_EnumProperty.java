package tgw.evolution.mixin;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DummyConstructor;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.patches.PatchEnumProperty;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.collection.sets.RHashSet;
import tgw.evolution.util.collection.sets.RSet;
import tgw.evolution.util.collection.sets.SimpleEnumSet;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("EqualsAndHashcode")
@Mixin(EnumProperty.class)
public abstract class Mixin_CF_EnumProperty<T extends Enum<T> & StringRepresentable> extends Property<T> implements PatchEnumProperty<T> {

    @Shadow @Final @DeleteField private Map<String, T> names;
    @Unique private final O2OMap<String, T> names_;
    @Shadow @Final @DeleteField private ImmutableSet<T> values;
    @Unique private final RSet<T> values_;

    @DummyConstructor
    public Mixin_CF_EnumProperty(String string, Class<T> class_, O2OMap<String, T> names_, RSet<T> values_) {
        super(string, class_);
        this.names_ = names_;
        this.values_ = values_;
    }

    @ModifyConstructor
    protected Mixin_CF_EnumProperty(String string, Class<T> class_, Collection<T> collection) {
        super(string, class_);
        this.names_ = new O2OHashMap<>();
        if (collection.size() <= 64) {
            this.values_ = SimpleEnumSet.of(class_, collection);
        }
        else {
            this.values_ = new RHashSet<>(collection);
        }
        for (long it = this.values_.beginIteration(); this.values_.hasNextIteration(it); it = this.values_.nextEntry(it)) {
            T value = this.values_.getIteration(it);
            String valueName = value.getSerializedName();
            if (this.names_.containsKey(valueName)) {
                throw new IllegalArgumentException("Multiple values have the same name '" + valueName + "'");
            }
            this.names_.put(valueName, value);
        }
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
        if (object instanceof EnumProperty enumProperty && super.equals(object)) {
            return this.values_.equals(enumProperty.values()) && this.names_.equals(enumProperty.names());
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
        return 31 * (31 * super.generateHashCode() + this.values_.hashCode()) + this.names_.hashCode();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Collection<T> getPossibleValues() {
        return this.values_;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Optional<T> getValue(String name) {
        Evolution.deprecatedMethod();
        return Optional.ofNullable(this.getValue_(name));
    }

    @Override
    public @Nullable T getValue_(String name) {
        return this.names_.get(name);
    }

    @Override
    public O2OMap<String, T> names() {
        return this.names_;
    }

    @Override
    public RSet<T> values() {
        return this.values_;
    }
}
