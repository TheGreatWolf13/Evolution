package tgw.evolution.mixin;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.collection.sets.ISet;
import tgw.evolution.util.collection.sets.RSet;

import java.util.Collection;
import java.util.Map;

@Mixin(StateDefinition.Builder.class)
public abstract class MixinStateDefinition_Builder<O, S extends StateHolder<O, S>> {

    @Shadow @Final private O owner;
    @Shadow @Final private Map<String, Property<?>> properties;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private <T extends Comparable<T>> void validateProperty(Property<T> property) {
        String propName = property.getName();
        if (!StateDefinition.NAME_PATTERN.matcher(propName).matches()) {
            throw new IllegalArgumentException(this.owner + " has invalidly named property: " + propName);
        }
        Collection<T> collection = property.getPossibleValues();
        if (collection.size() <= 1) {
            throw new IllegalArgumentException(this.owner + " attempted use property " + propName + " with <= 1 possible values");
        }
        if (property instanceof IntegerProperty p) {
            ISet set = (ISet) collection;
            for (long it = set.beginIteration(); set.hasNextIteration(it); it = set.nextEntry(it)) {
                int value = set.getIteration(it);
                String name = p.getName(value);
                if (StateDefinition.NAME_PATTERN.matcher(name).matches()) {
                    continue;
                }
                throw new IllegalArgumentException(this.owner + " has property: " + propName + " with invalidly named value: " + name);
            }
            if (this.properties.containsKey(propName)) {
                throw new IllegalArgumentException(this.owner + " has duplicate property: " + propName);
            }
            return;
        }
        if (property instanceof BooleanProperty p) {
            String name = p.getName(false);
            if (!StateDefinition.NAME_PATTERN.matcher(name).matches()) {
                throw new IllegalArgumentException(this.owner + " has property: " + propName + " with invalidly named value: " + name);
            }
            name = p.getName(true);
            if (!StateDefinition.NAME_PATTERN.matcher(name).matches()) {
                throw new IllegalArgumentException(this.owner + " has property: " + propName + " with invalidly named value: " + name);
            }
            if (this.properties.containsKey(propName)) {
                throw new IllegalArgumentException(this.owner + " has duplicate property: " + propName);
            }
            return;
        }
        if (property instanceof EnumProperty p) {
            RSet<T> set = (RSet<T>) collection;
            for (long it = set.beginIteration(); set.hasNextIteration(it); it = set.nextEntry(it)) {
                T value = set.getIteration(it);
                String name = p.getName((Enum) value);
                if (StateDefinition.NAME_PATTERN.matcher(name).matches()) {
                    continue;
                }
                throw new IllegalArgumentException(this.owner + " has property: " + propName + " with invalidly named value: " + name);
            }
            if (this.properties.containsKey(propName)) {
                throw new IllegalArgumentException(this.owner + " has duplicate property: " + propName);
            }
            return;
        }
        throw new UnregisteredFeatureException("Property not registered for validation: \"" + propName + "\". Class: " + property.getClass().getCanonicalName());
    }
}
