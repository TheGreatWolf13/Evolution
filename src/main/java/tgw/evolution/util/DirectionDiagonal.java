package tgw.evolution.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

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

    public void movePos(BlockPos.MutableBlockPos mutablePos) {
        mutablePos.move(this.xDirection).move(this.zDirection);
    }
}
