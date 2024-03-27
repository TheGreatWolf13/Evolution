package tgw.evolution.mixin;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.patches.PatchBooleanProperty;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("EqualsAndHashcode")
@Mixin(BooleanProperty.class)
public abstract class Mixin_CF_BooleanProperty extends Property<Boolean> implements PatchBooleanProperty {

    @Shadow @Final @DeleteField private ImmutableSet<Boolean> values;

    @ModifyConstructor
    public Mixin_CF_BooleanProperty(String name) {
        super(name, Boolean.class);
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
        return object instanceof BooleanProperty && super.equals(object);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public int generateHashCode() {
        return 31 * super.generateHashCode();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public String getName(Boolean value) {
        return this.getName(value.booleanValue());
    }

    @Override
    public String getName(boolean value) {
        return String.valueOf(value);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Collection<Boolean> getPossibleValues() {
        return PatchBooleanProperty.VALUES;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Optional<Boolean> getValue(String name) {
        Evolution.deprecatedMethod();
        return Optional.ofNullable(this.getValue_(name));
    }

    @Override
    public @Nullable Boolean getValue_(String name) {
        if ("true".equals(name)) {
            return Boolean.TRUE;
        }
        if ("false".equals(name)) {
            return Boolean.FALSE;
        }
        return null;
    }
}
