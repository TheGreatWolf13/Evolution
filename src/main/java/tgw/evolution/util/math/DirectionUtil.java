package tgw.evolution.util.math;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;

import java.util.random.RandomGenerator;

public final class DirectionUtil {

    public static final Direction.Axis[] AXIS = Direction.Axis.values();
    public static final Direction[] ALL = Direction.values();
    public static final Direction[] ALL_EXCEPT_DOWN = {Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] HORIZ_NESW = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] HORIZ_WENS = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
    public static final Direction[] NSWEU = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP};
    public static final Direction[] UNESW = {Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] UPDATE_ORDER = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
    public static final Direction[] X = {Direction.WEST, Direction.EAST};
    public static final Direction[] Z = {Direction.NORTH, Direction.SOUTH};

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

    public static double choose(Direction.Axis axis, double x, double y, double z) {
        return switch (axis) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }

    public static Direction chooseByDistance(double negativeOff, double positiveOff, Direction.Axis primaryAxis, double primaryDist, Direction.Axis secondaryAxis, double secondaryDist, Direction fallback, int dirSet) {
        assert negativeOff < positiveOff;
        assert negativeOff > 0;
        assert positiveOff < 1;
        assert 0 <= primaryDist && primaryDist <= 1;
        assert 0 <= secondaryDist && secondaryDist <= 1;
        if (primaryDist < negativeOff) {
            if (secondaryDist < negativeOff) {
                if (primaryDist < secondaryDist) {
                    Direction negative = getNegative(primaryAxis);
                    if (hasDirection(dirSet, negative)) {
                        return negative;
                    }
                }
                Direction negative = getNegative(secondaryAxis);
                if (hasDirection(dirSet, negative)) {
                    return negative;
                }
            }
            else if (secondaryDist > positiveOff) {
                if (primaryDist < 1 - secondaryDist) {
                    Direction negative = getNegative(primaryAxis);
                    if (hasDirection(dirSet, negative)) {
                        return negative;
                    }
                }
                Direction positive = getPositive(secondaryAxis);
                if (hasDirection(dirSet, positive)) {
                    return positive;
                }
            }
            else {
                Direction negative = getNegative(primaryAxis);
                if (hasDirection(dirSet, negative)) {
                    return negative;
                }
            }
        }
        else if (primaryDist > positiveOff) {
            if (secondaryDist < negativeOff) {
                if (1 - primaryDist < secondaryDist) {
                    Direction positive = getPositive(primaryAxis);
                    if (hasDirection(dirSet, positive)) {
                        return positive;
                    }
                }
                Direction negative = getNegative(secondaryAxis);
                if (hasDirection(dirSet, negative)) {
                    return negative;
                }
            }
            else if (secondaryDist > positiveOff) {
                if (primaryDist > secondaryDist) {
                    Direction positive = getPositive(primaryAxis);
                    if (hasDirection(dirSet, positive)) {
                        return positive;
                    }
                }
                Direction positive = getPositive(secondaryAxis);
                if (hasDirection(dirSet, positive)) {
                    return positive;
                }
            }
            else {
                Direction positive = getPositive(primaryAxis);
                if (hasDirection(dirSet, positive)) {
                    return positive;
                }
            }
        }
        else if (secondaryDist < negativeOff) {
            Direction negative = getNegative(secondaryAxis);
            if (hasDirection(dirSet, negative)) {
                return negative;
            }
        }
        else if (secondaryDist > positiveOff) {
            Direction positive = getPositive(secondaryAxis);
            if (hasDirection(dirSet, positive)) {
                return positive;
            }
        }
        return fallback;
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

    public static Direction fromLocalToAbs(Direction local, Direction facing) {
        int index = horizontalIndex(local) + horizontalIndex(facing);
        if (index > 3) {
            index -= 4;
        }
        return HORIZ_NESW[index];
    }

    public static int fullSetExcept(Direction dir) {
        return switch (dir) {
            case NORTH -> 0b11_1011;
            case SOUTH -> 0b11_0111;
            case UP -> 0b11_1101;
            case DOWN -> 0b11_1110;
            case EAST -> 0b01_1111;
            case WEST -> 0b10_1111;
        };
    }

    public static Direction getNegative(Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.WEST;
            case Y -> Direction.DOWN;
            case Z -> Direction.NORTH;
        };
    }

    public static Direction getPositive(Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.EAST;
            case Y -> Direction.UP;
            case Z -> Direction.SOUTH;
        };
    }

    public static Direction getRandom(RandomGenerator random) {
        return ALL[random.nextInt(6)];
    }

    public static boolean hasDirection(int dirSet, Direction dir) {
        return (dirSet & 1 << dir.ordinal()) != 0;
    }

    public static int horizontalIndex(Direction dir) {
        return switch (dir) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> throw new IllegalStateException("Only horizontal directions!");
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
