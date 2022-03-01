package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "net.minecraft.core.AxisCycle$3")
public abstract class AxisCycle_BackwardMixin {

    /**
     * @author JellySquid
     * <p>
     * Avoid expensive array/modulo operations
     */
    @Overwrite
    public Direction.Axis cycle(Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.Axis.Z;
            case Y -> Direction.Axis.X;
            case Z -> Direction.Axis.Y;
        };
    }
}
