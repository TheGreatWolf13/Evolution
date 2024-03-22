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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchBlockHitResult;
import tgw.evolution.patches.PatchVoxelShape;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.math.VectorUtil;

import java.util.List;
import java.util.Optional;

@Mixin(VoxelShape.class)
public abstract class MixinVoxelShape implements PatchVoxelShape {

    @Shadow @Final protected DiscreteVoxelShape shape;
    @Unique private @Nullable OList<AABB> cachedBoxes;

    @Override
    public @UnmodifiableView OList<AABB> cachedBoxes() {
        if (this.cachedBoxes == null) {
            this.makeCache();
        }
        return this.cachedBoxes;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations when possible.
     */
    @Overwrite
    public @Nullable BlockHitResult clip(Vec3 start, Vec3 end, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.clip_(start, end, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable BlockHitResult clip_(Vec3 start, Vec3 end, int x, int y, int z) {
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
        if (this.shape.isFullWide(this.findIndex(Direction.Axis.X, incX - x),
                                  this.findIndex(Direction.Axis.Y, incY - y),
                                  this.findIndex(Direction.Axis.Z, incZ - z))) {
            return PatchBlockHitResult.create(incX, incY, incZ, Direction.getNearest(dx, dy, dz).getOpposite(), x, y, z, true);
        }
        double[] ds = {1.0};
        Direction[] direction = {null};
        this.forAllBoxes((x0, y0, z0, x1, y1, z1) -> {
            if (dx > 1.0E-7) {
                direction[0] = AABB.clipPoint(ds, direction[0], dx, dy, dz,
                                              x0 + x, y0 + y, y1 + y, z0 + z, z1 + z, Direction.WEST, start.x, start.y, start.z);
            }
            else if (dx < -1.0E-7) {
                direction[0] = AABB.clipPoint(ds, direction[0], dx, dy, dz,
                                              x1 + x, y0 + y, y1 + y, z0 + z, z1 + z, Direction.EAST, start.x, start.y, start.z);
            }
            if (dy > 1.0E-7) {
                direction[0] = AABB.clipPoint(ds, direction[0], dy, dz, dx,
                                              y0 + y, z0 + z, z1 + z, x0 + x, x1 + x, Direction.DOWN, start.y, start.z, start.x);
            }
            else if (dy < -1.0E-7) {
                direction[0] = AABB.clipPoint(ds, direction[0], dy, dz, dx,
                                              y1 + y, z0 + z, z1 + z, x0 + x, x1 + x, Direction.UP, start.y, start.z, start.x);
            }
            if (dz > 1.0E-7) {
                direction[0] = AABB.clipPoint(ds, direction[0], dz, dx, dy,
                                              z0 + z, x0 + x, x1 + x, y0 + y, y1 + y, Direction.NORTH, start.z, start.x, start.y);
            }
            else if (dz < -1.0E-7) {
                direction[0] = AABB.clipPoint(ds, direction[0], dz, dx, dy,
                                              z1 + z, x0 + x, x1 + x, y0 + y, y1 + y, Direction.SOUTH, start.z, start.x, start.y);
            }
        });
        if (direction[0] == null) {
            return null;
        }
        double dist = ds[0];
        return PatchBlockHitResult.create(start.x + dist * dx, start.y + dist * dy, start.z + dist * dz, direction[0], x, y, z, false);
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
    public double collideX(AxisCycle cycle, AABB collisionBox, double desiredOffset) {
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

    @Unique
    private void makeCache() {
        if (this.isEmpty()) {
            this.cachedBoxes = OList.emptyList();
        }
        else {
            OList<AABB> list = new OArrayList<>();
            this.forAllBoxes((x0, y0, z0, x1, y1, z1) -> list.add(new AABB(x0, y0, z0, x1, y1, z1)));
            list.trimCollection();
            this.cachedBoxes = list.view();
        }
    }

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

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public List<AABB> toAabbs() {
        Evolution.deprecatedMethod();
        return this.cachedBoxes();
    }
}
