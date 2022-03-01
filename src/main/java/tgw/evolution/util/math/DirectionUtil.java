package tgw.evolution.util.math;

import net.minecraft.core.Direction;

import java.util.Arrays;

public final class DirectionUtil {

    public static final Direction[] ALL = Direction.values();

    // Provides the same order as enumerating Direction and checking the axis of each value
    public static final Direction[] HORIZ_NSWE = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    public static final Direction[] HORIZ_NESW = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] ALL_EXCEPT_DOWN = {Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] ALL_EXCEPT_UP = {Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] X = {Direction.WEST, Direction.EAST};
    public static final Direction[] Z = {Direction.NORTH, Direction.SOUTH};
    private static final Direction[] OPPOSITE = Arrays.stream(ALL).map(Direction::getOpposite).toArray(Direction[]::new);

    private DirectionUtil() {
    }

    public static Direction getOpposite(Direction dir) {
        return OPPOSITE[dir.ordinal()];
    }
}
