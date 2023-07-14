package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "net.minecraft.core.AxisCycle$2")
public abstract class MixinAxisCycle_Forward {

    /**
     * @author JellySquid
     * @reason Avoid expensive array/modulo operations
     */
    @Overwrite
    public Direction.Axis cycle(Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.Axis.Y;
            case Y -> Direction.Axis.Z;
            case Z -> Direction.Axis.X;
        };
    }
}
