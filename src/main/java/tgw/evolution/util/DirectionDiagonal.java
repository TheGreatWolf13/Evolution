package tgw.evolution.util;

import net.minecraft.util.Direction;

public enum DirectionDiagonal {
    NORTH_WEST(Direction.WEST, Direction.NORTH),
    NORTH_EAST(Direction.EAST, Direction.NORTH),
    SOUTH_WEST(Direction.WEST, Direction.SOUTH),
    SOUTH_EAST(Direction.EAST, Direction.SOUTH);

    private final Direction xDirection;
    private final Direction zDirection;

    DirectionDiagonal(Direction xDirection, Direction zDirection) {
        this.xDirection = xDirection;
        this.zDirection = zDirection;
    }

    public Direction getxDirection() {
        return this.xDirection;
    }

    public Direction getzDirection() {
        return this.zDirection;
    }
}
