package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AABB.class)
public abstract class AABBMixin {

    @Shadow
    @Final
    public double maxX;
    @Shadow
    @Final
    public double maxY;
    @Shadow
    @Final
    public double maxZ;
    @Shadow
    @Final
    public double minX;
    @Shadow
    @Final
    public double minY;
    @Shadow
    @Final
    public double minZ;

    /**
     * @author MGSchultz
     * <p>
     * Simplify the code to better help the JVM optimize it
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
     * @author MGSchultz
     * <p>
     * Simplify the code to better help the JVM optimize it
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
