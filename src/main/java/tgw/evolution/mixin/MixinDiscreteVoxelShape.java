package tgw.evolution.mixin;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.math.DirectionUtil;

@Mixin(DiscreteVoxelShape.class)
public abstract class MixinDiscreteVoxelShape {

    @Shadow @Final protected int xSize;
    @Shadow @Final protected int ySize;
    @Shadow @Final protected int zSize;

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public int firstFull(Direction.Axis axis, int y, int z) {
        int size = this.getSize(axis);
        if (y >= 0 && z >= 0) {
            if (y < this.getSize(DirectionUtil.forward(axis)) && z < this.getSize(DirectionUtil.backward(axis))) {
                AxisCycle cycle = switch (axis) {
                    case X -> AxisCycle.NONE;
                    case Y -> AxisCycle.FORWARD;
                    case Z -> AxisCycle.BACKWARD;
                };
                for (int i = 0; i < size; ++i) {
                    if (this.isFull(cycle, i, y, z)) {
                        return i;
                    }
                }
                return size;
            }
            return size;
        }
        return size;
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline some methods.
     */
    @Overwrite
    private void forAllAxisEdges(DiscreteVoxelShape.IntLineConsumer lineConsumer, AxisCycle cycle, boolean combine) {
        cycle = DirectionUtil.inverse(cycle);
        int sizeX;
        int sizeY;
        int sizeZ;
        switch (cycle) {
            case NONE -> {
                sizeX = this.getXSize();
                sizeY = this.getYSize();
                sizeZ = this.getZSize();
            }
            case FORWARD -> {
                sizeX = this.getYSize();
                sizeY = this.getZSize();
                sizeZ = this.getXSize();
            }
            case BACKWARD -> {
                sizeX = this.getZSize();
                sizeY = this.getXSize();
                sizeZ = this.getYSize();
            }
            default -> throw new IncompatibleClassChangeError();
        }
        for (int x = 0; x <= sizeX; ++x) {
            for (int y = 0; y <= sizeY; ++y) {
                int i = -1;
                for (int z = 0; z <= sizeZ; ++z) {
                    int l1 = 0;
                    int i2 = 0;
                    for (int dx = 0; dx <= 1; ++dx) {
                        for (int dy = 0; dy <= 1; ++dy) {
                            if (this.isFullWide(cycle, x + dx - 1, y + dy - 1, z)) {
                                ++l1;
                                i2 ^= dx ^ dy;
                            }
                        }
                    }
                    if (l1 == 1 || l1 == 3 || l1 == 2 && (i2 & 1) == 0) {
                        if (combine) {
                            if (i == -1) {
                                i = z;
                            }
                        }
                        else {
                            switch (cycle) {
                                case NONE -> lineConsumer.consume(x, y, z, x, y, z + 1);
                                case FORWARD -> lineConsumer.consume(z, x, y, z + 1, x, y);
                                case BACKWARD -> lineConsumer.consume(y, z, x, y, z + 1, x);
                            }
                        }
                    }
                    else if (i != -1) {
                        switch (cycle) {
                            case NONE -> lineConsumer.consume(x, y, i, x, y, z);
                            case FORWARD -> lineConsumer.consume(i, x, y, z, x, y);
                            case BACKWARD -> lineConsumer.consume(y, i, x, y, z, x);
                        }
                        i = -1;
                    }
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline some methods.
     */
    @Overwrite
    private void forAllAxisFaces(DiscreteVoxelShape.IntFaceConsumer faceConsumer, AxisCycle cycle) {
        cycle = DirectionUtil.inverse(cycle);
        int sizeX;
        int sizeY;
        int sizeZ;
        Direction positiveDir;
        Direction negativeDir;
        switch (cycle) {
            case NONE -> {
                sizeX = this.getXSize();
                sizeY = this.getYSize();
                sizeZ = this.getZSize();
                positiveDir = Direction.SOUTH;
                negativeDir = Direction.NORTH;
            }
            case FORWARD -> {
                sizeX = this.getYSize();
                sizeY = this.getZSize();
                sizeZ = this.getXSize();
                positiveDir = Direction.EAST;
                negativeDir = Direction.WEST;
            }
            case BACKWARD -> {
                sizeX = this.getZSize();
                sizeY = this.getXSize();
                sizeZ = this.getYSize();
                positiveDir = Direction.UP;
                negativeDir = Direction.DOWN;
            }
            default -> throw new IncompatibleClassChangeError();
        }
        for (int x = 0; x < sizeX; ++x) {
            for (int y = 0; y < sizeY; ++y) {
                boolean f = false;
                for (int z = 0; z <= sizeZ; ++z) {
                    boolean b = z != sizeZ && this.isFull(cycle, x, y, z);
                    if (!f && b) {
                        switch (cycle) {
                            case NONE -> faceConsumer.consume(negativeDir, x, y, z);
                            case FORWARD -> faceConsumer.consume(negativeDir, z, x, y);
                            case BACKWARD -> faceConsumer.consume(negativeDir, y, z, x);
                        }
                    }
                    if (f && !b) {
                        switch (cycle) {
                            case NONE -> faceConsumer.consume(positiveDir, x, y, z - 1);
                            case FORWARD -> faceConsumer.consume(positiveDir, z - 1, x, y);
                            case BACKWARD -> faceConsumer.consume(positiveDir, y, z - 1, x);
                        }
                    }
                    f = b;
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public int getSize(Direction.Axis axis) {
        return switch (axis) {
            case X -> this.xSize;
            case Y -> this.ySize;
            case Z -> this.zSize;
        };
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public int getXSize() {
        return this.xSize;
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public int getYSize() {
        return this.ySize;
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public int getZSize() {
        return this.zSize;
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public boolean isFull(AxisCycle cycle, int x, int y, int z) {
        return switch (cycle) {
            case NONE -> this.isFull(x, y, z);
            case FORWARD -> this.isFull(z, x, y);
            case BACKWARD -> this.isFull(y, z, x);
        };
    }

    @Shadow
    public abstract boolean isFull(int pX, int pY, int pZ);

    @Shadow
    public abstract boolean isFullWide(int pX, int pY, int pZ);

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public boolean isFullWide(AxisCycle cycle, int x, int y, int z) {
        return switch (cycle) {
            case NONE -> this.isFullWide(x, y, z);
            case FORWARD -> this.isFullWide(z, x, y);
            case BACKWARD -> this.isFullWide(y, z, x);
        };
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public int lastFull(Direction.Axis axis, int y, int z) {
        if (y >= 0 && z >= 0) {
            if (y < this.getSize(DirectionUtil.forward(axis)) && z < this.getSize(DirectionUtil.backward(axis))) {
                int size = this.getSize(axis);
                AxisCycle cycle = switch (axis) {
                    case X -> AxisCycle.NONE;
                    case Y -> AxisCycle.FORWARD;
                    case Z -> AxisCycle.BACKWARD;
                };
                for (int i = size - 1; i >= 0; --i) {
                    if (this.isFull(cycle, i, y, z)) {
                        return i + 1;
                    }
                }
                return 0;
            }
            return 0;
        }
        return 0;
    }
}
