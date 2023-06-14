package tgw.evolution.util.math;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;

public final class DirectionUtil {

    public static final Direction[] ALL = Direction.values();
    public static final Direction[] HORIZ_NESW = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] ALL_EXCEPT_DOWN = {Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] X = {Direction.WEST, Direction.EAST};
    public static final Direction[] Z = {Direction.NORTH, Direction.SOUTH};
    public static final Direction[] HORIZ_WENS = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

    private DirectionUtil() {
    }

    public static Direction.Axis backward(Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.Axis.Z;
            case Y -> Direction.Axis.X;
            case Z -> Direction.Axis.Y;
        };
    }

    public static int choose(Direction.Axis axis, int x, int y, int z) {
        return switch (axis) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }

    public static int cycle(AxisCycle cycle, int x, int y, int z, Direction.Axis axis) {
        return switch (cycle) {
            case NONE -> choose(axis, x, y, z);
            case FORWARD -> choose(axis, z, x, y);
            case BACKWARD -> choose(axis, y, z, x);
        };
    }

    public static Direction.Axis cycle(AxisCycle cycle, Direction.Axis axis) {
        return switch (cycle) {
            case NONE -> axis;
            case FORWARD -> forward(axis);
            case BACKWARD -> backward(axis);
        };
    }

    public static Direction.Axis forward(Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.Axis.Y;
            case Y -> Direction.Axis.Z;
            case Z -> Direction.Axis.X;
        };
    }

    public static AxisCycle inverse(AxisCycle cycle) {
        return switch (cycle) {
            case NONE -> AxisCycle.NONE;
            case FORWARD -> AxisCycle.BACKWARD;
            case BACKWARD -> AxisCycle.FORWARD;
        };
    }
}
