package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3.class)
public abstract class MixinVec3 {

    @Shadow @Final public double x;

    @Shadow @Final public double y;

    @Shadow @Final public double z;

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public double get(Direction.Axis axis) {
        return switch (axis) {
            case X -> this.x;
            case Y -> this.y;
            case Z -> this.z;
        };
    }
}
