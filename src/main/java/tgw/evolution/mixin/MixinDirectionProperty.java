package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.sets.RSet;
import tgw.evolution.util.collection.sets.SimpleEnumSet;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Collection;
import java.util.function.Predicate;

@Mixin(DirectionProperty.class)
public abstract class MixinDirectionProperty extends EnumProperty<Direction> {

    public MixinDirectionProperty(String string, Class<Direction> class_, Collection<Direction> collection) {
        super(string, class_, collection);
    }

    @Shadow
    public static DirectionProperty create(String string, Collection<Direction> collection) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static DirectionProperty create(String string, Predicate<Direction> predicate) {
        RSet<Direction> set = new SimpleEnumSet<>(Direction.class);
        for (Direction direction : DirectionUtil.ALL) {
            if (predicate.test(direction)) {
                set.add(direction);
            }
        }
        return create(string, set);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @SuppressWarnings("OverwriteModifiers")
    @Overwrite
    public static DirectionProperty create(String string, Direction... directions) {
        return create(string, SimpleEnumSet.of(directions));
    }
}
