package tgw.evolution.mixin;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AxisCycle.class)
public abstract class AxisCycleMixin {

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public static AxisCycle between(Direction.Axis from, Direction.Axis to) {
        if (from == to) {
            return AxisCycle.NONE;
        }
        int delta = to.ordinal() - from.ordinal();
        if (delta == 1 || delta == -2) {
            return AxisCycle.FORWARD;
        }
        return AxisCycle.BACKWARD;
    }
}
