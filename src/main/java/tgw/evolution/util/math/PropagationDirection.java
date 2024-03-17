package tgw.evolution.util.math;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.random.RandomGenerator;

public enum PropagationDirection {
    NONE(0, 0, 0),
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0),
    DOWN_NORTH(0, -1, -1),
    DOWN_SOUTH(0, -1, 1),
    DOWN_WEST(-1, -1, 0),
    DOWN_EAST(1, -1, 0);

    public static final PropagationDirection[] VALUES = values();
    public static final PropagationDirection[] HORIZ_NESW = {NORTH, EAST, SOUTH, WEST};
    public static final PropagationDirection[][] HORIZ = {HORIZ_NESW, {NORTH, EAST, WEST, SOUTH}, {NORTH, SOUTH, EAST, WEST}, {NORTH, SOUTH, WEST, EAST}, {NORTH, WEST, SOUTH, EAST}, {NORTH, WEST, EAST, SOUTH},
                                                          {SOUTH, EAST, NORTH, WEST}, {SOUTH, EAST, WEST, NORTH}, {SOUTH, NORTH, EAST, WEST}, {SOUTH, NORTH, WEST, EAST}, {SOUTH, WEST, NORTH, EAST}, {SOUTH, WEST, EAST, NORTH},
                                                          {EAST, SOUTH, NORTH, WEST}, {EAST, SOUTH, WEST, NORTH}, {EAST, NORTH, SOUTH, WEST}, {EAST, NORTH, WEST, SOUTH}, {EAST, WEST, NORTH, SOUTH}, {EAST, WEST, SOUTH, NORTH},
                                                          {WEST, EAST, NORTH, SOUTH}, {WEST, EAST, SOUTH, NORTH}, {WEST, NORTH, EAST, SOUTH}, {WEST, NORTH, SOUTH, EAST}, {WEST, SOUTH, NORTH, EAST}, {WEST, SOUTH, EAST, NORTH}};
    public final int stepX;
    public final int stepY;
    public final int stepZ;

    PropagationDirection(int stepX, int stepY, int stepZ) {
        this.stepX = stepX;
        this.stepY = stepY;
        this.stepZ = stepZ;
    }

    public static PropagationDirection fromDirection(Direction dir) {
        return switch (dir) {
            case UP -> UP;
            case DOWN -> DOWN;
            case EAST -> EAST;
            case WEST -> WEST;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
        };
    }

    public static PropagationDirection fromDirectionOffsetDown(Direction dir) {
        return switch (dir) {
            case UP -> NONE;
            case DOWN -> DOWN;
            case EAST -> DOWN_EAST;
            case WEST -> DOWN_WEST;
            case NORTH -> DOWN_NORTH;
            case SOUTH -> DOWN_SOUTH;
        };
    }

    public static PropagationDirection[] randomHorizontal(RandomGenerator random) {
        return HORIZ[random.nextInt(24)];
    }

    public @Nullable Direction getClosestHorizontal() {
        return switch (this) {
            case EAST, DOWN_EAST -> Direction.EAST;
            case WEST, DOWN_WEST -> Direction.WEST;
            case NORTH, DOWN_NORTH -> Direction.NORTH;
            case SOUTH, DOWN_SOUTH -> Direction.SOUTH;
            case NONE, DOWN, UP -> null;
        };
    }
}
