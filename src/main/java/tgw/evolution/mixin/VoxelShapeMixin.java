package tgw.evolution.mixin;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.math.VectorUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Mixin(VoxelShape.class)
public abstract class VoxelShapeMixin {

    @Shadow
    @Final
    protected DiscreteVoxelShape shape;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations when possible.
     */
    @Overwrite
    @Nullable
    public BlockHitResult clip(Vec3 start, Vec3 end, BlockPos pos) {
        if (this.isEmpty()) {
            return null;
        }
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double dz = end.z - start.z;
        if (VectorUtil.lengthSqr(dx, dy, dz) < 1.0E-7) {
            return null;
        }
        double incX = start.x + dx * 0.001;
        double incY = start.y + dy * 0.001;
        double incZ = start.z + dz * 0.001;
        return this.shape.isFullWide(this.findIndex(Direction.Axis.X, incX - pos.getX()),
                                     this.findIndex(Direction.Axis.Y, incY - pos.getY()),
                                     this.findIndex(Direction.Axis.Z, incZ - pos.getZ())) ?
               new BlockHitResult(new Vec3d(incX, incY, incZ), Direction.getNearest(dx, dy, dz).getOpposite(), pos, true) :
               AABB.clip(this.toAabbs(), start, end, pos);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations when possible.
     */
    @Overwrite
    public Optional<Vec3> closestPointTo(Vec3 point) {
        if (this.isEmpty()) {
            return Optional.empty();
        }
        Vec3d result = new Vec3d(Vec3d.NULL);
        this.forAllBoxes((x0, y0, z0, x1, y1, z1) -> {
            double x = Mth.clamp(point.x(), x0, x1);
            double y = Mth.clamp(point.y(), y0, y1);
            double z = Mth.clamp(point.z(), z0, z1);
            if (result.isNull() || point.distanceToSqr(x, y, z) < point.distanceToSqr(result)) {
                result.set(x, y, z);
            }
        });
        return Optional.of(result);
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public double collide(Direction.Axis movementAxis, AABB collisionBox, double desiredOffset) {
        AxisCycle cycle = switch (movementAxis) {
            case X -> AxisCycle.NONE;
            case Y -> AxisCycle.BACKWARD;
            case Z -> AxisCycle.FORWARD;
        };
        return this.collideX(cycle, collisionBox, desiredOffset);
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    protected double collideX(AxisCycle cycle, AABB collisionBox, double desiredOffset) {
        if (this.isEmpty()) {
            return desiredOffset;
        }
        if (Math.abs(desiredOffset) < 1.0E-7) {
            return 0;
        }
        cycle = DirectionUtil.inverse(cycle);
        Direction.Axis xAxis;
        Direction.Axis yAxis;
        Direction.Axis zAxis;
        switch (cycle) {
            case NONE -> {
                xAxis = Direction.Axis.X;
                yAxis = Direction.Axis.Y;
                zAxis = Direction.Axis.Z;
            }
            case FORWARD -> {
                xAxis = Direction.Axis.Y;
                yAxis = Direction.Axis.Z;
                zAxis = Direction.Axis.X;
            }
            case BACKWARD -> {
                xAxis = Direction.Axis.Z;
                yAxis = Direction.Axis.X;
                zAxis = Direction.Axis.Y;
            }
            default -> throw new IncompatibleClassChangeError();
        }
        int minY = Math.max(0, this.findIndex(yAxis, collisionBox.min(yAxis) + 1.0E-7));
        int maxY = Math.min(this.shape.getSize(yAxis), this.findIndex(yAxis, collisionBox.max(yAxis) - 1.0E-7) + 1);
        int minZ = Math.max(0, this.findIndex(zAxis, collisionBox.min(zAxis) + 1.0E-7));
        int maxZ = Math.min(this.shape.getSize(zAxis), this.findIndex(zAxis, collisionBox.max(zAxis) - 1.0E-7) + 1);
        if (desiredOffset > 0) {
            double maxX = collisionBox.max(xAxis);
            int xSize = this.shape.getSize(xAxis);
            for (int x = this.findIndex(xAxis, maxX - 1.0E-7) + 1; x < xSize; ++x) {
                for (int y = minY; y < maxY; ++y) {
                    for (int z = minZ; z < maxZ; ++z) {
                        if (this.shape.isFullWide(cycle, x, y, z)) {
                            double d = this.get(xAxis, x) - maxX;
                            if (d >= -1.0E-7) {
                                return Math.min(desiredOffset, d);
                            }
                            return desiredOffset;
                        }
                    }
                }
            }
        }
        else if (desiredOffset < 0) {
            double minX = collisionBox.min(xAxis);
            for (int x = this.findIndex(xAxis, minX + 1.0E-7) - 1; x >= 0; --x) {
                for (int y = minY; y < maxY; ++y) {
                    for (int z = minZ; z < maxZ; ++z) {
                        if (this.shape.isFullWide(cycle, x, y, z)) {
                            double d = this.get(xAxis, x + 1) - minX;
                            if (d <= 1.0E-7) {
                                return Math.max(desiredOffset, d);
                            }
                            return desiredOffset;
                        }
                    }
                }
            }
        }
        return desiredOffset;
    }

    @Shadow
    protected abstract int findIndex(Direction.Axis pAxis, double pPosition);

    @Shadow
    public abstract void forAllBoxes(Shapes.DoubleLineConsumer pAction);

    @Shadow
    protected abstract double get(Direction.Axis pAxis, int pIndex);

    @Shadow
    public abstract boolean isEmpty();

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public double max(Direction.Axis axis, double primaryPos, double secondaryPos) {
        int i = this.findIndex(DirectionUtil.forward(axis), primaryPos);
        int j = this.findIndex(DirectionUtil.backward(axis), secondaryPos);
        int k = this.shape.lastFull(axis, i, j);
        return k <= 0 ? Double.NEGATIVE_INFINITY : this.get(axis, k);
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public double min(Direction.Axis axis, double primaryPos, double secondaryPos) {
        int i = this.findIndex(DirectionUtil.forward(axis), primaryPos);
        int j = this.findIndex(DirectionUtil.backward(axis), secondaryPos);
        int k = this.shape.firstFull(axis, i, j);
        return k >= this.shape.getSize(axis) ? Double.POSITIVE_INFINITY : this.get(axis, k);
    }

    @Shadow
    public abstract List<AABB> toAabbs();
}
