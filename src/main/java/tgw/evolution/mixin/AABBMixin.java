package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.patches.IAABBPatch;

@Mixin(AABB.class)
public abstract class AABBMixin implements IAABBPatch {

    @Mutable
    @Shadow
    @Final
    public double maxX;
    @Mutable
    @Shadow
    @Final
    public double maxY;
    @Mutable
    @Shadow
    @Final
    public double maxZ;
    @Mutable
    @Shadow
    @Final
    public double minX;
    @Mutable
    @Shadow
    @Final
    public double minY;
    @Mutable
    @Shadow
    @Final
    public double minZ;

    /**
     * @author TheGreatWolf
     * @reason Simplify the code to better help the JVM optimize it
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
     * @reason Simplify the code to better help the JVM optimize it
     */
    @Overwrite
    public double min(Direction.Axis axis) {
        return switch (axis) {
            case X -> this.minX;
            case Y -> this.minY;
            case Z -> this.minZ;
        };
    }

    @Override
    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    @Override
    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    @Override
    public void setMaxZ(double maxZ) {
        this.maxZ = maxZ;
    }

    @Override
    public void setMinX(double minX) {
        this.minX = minX;
    }

    @Override
    public void setMinY(double minY) {
        this.minY = minY;
    }

    @Override
    public void setMinZ(double minZ) {
        this.minZ = minZ;
    }
}
