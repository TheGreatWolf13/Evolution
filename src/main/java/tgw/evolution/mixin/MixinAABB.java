package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.util.physics.EarthHelper;

@Mixin(AABB.class)
public abstract class MixinAABB {

    @Mutable @Shadow @Final public double maxX;
    @Mutable @Shadow @Final public double maxY;
    @Mutable @Shadow @Final public double maxZ;
    @Mutable @Shadow @Final public double minX;
    @Mutable @Shadow @Final public double minY;
    @Mutable @Shadow @Final public double minZ;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        //SAT, but with the wrap distance equation
        return EarthHelper.deltaBlockCoordinate(this.minX, maxX) < 0 && EarthHelper.deltaBlockCoordinate(minX, this.maxX) < 0 && this.minY - maxY < 0 && minY - this.maxY < 0 && EarthHelper.deltaBlockCoordinate(this.minZ, maxZ) < 0 && EarthHelper.deltaBlockCoordinate(minZ, this.maxZ) < 0;
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public double max(Direction.Axis axis) {
        return switch (axis) {
            case X -> this.maxX;
            case Y -> this.maxY;
            case Z -> this.maxZ;
        };
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public double min(Direction.Axis axis) {
        return switch (axis) {
            case X -> this.minX;
            case Y -> this.minY;
            case Z -> this.minZ;
        };
    }
}
