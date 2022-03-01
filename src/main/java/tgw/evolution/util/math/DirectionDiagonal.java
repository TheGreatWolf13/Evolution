package tgw.evolution.util.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

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

    public static DirectionDiagonal sum(Direction dir1, Direction dir2) {
        if (dir1.getAxis().isVertical() || dir2.getAxis().isVertical()) {
            throw new IllegalStateException("Should not be used with vertical directions");
        }
        if (dir1.getAxis() == dir2.getAxis()) {
            throw new IllegalStateException("Should not be used with directions on the same axis");
        }
        boolean isFirstX = dir1.getAxis() == Direction.Axis.X;
        Direction dirX = isFirstX ? dir1 : dir2;
        Direction dirZ = isFirstX ? dir2 : dir1;
        if (dirX == Direction.WEST) {
            if (dirZ == Direction.NORTH) {
                return NORTH_WEST;
            }
            return SOUTH_WEST;
        }
        if (dirZ == Direction.NORTH) {
            return NORTH_EAST;
        }
        return SOUTH_EAST;
    }

    public void movePos(BlockPos.MutableBlockPos mutablePos) {
        mutablePos.move(this.xDirection).move(this.zDirection);
    }
}
