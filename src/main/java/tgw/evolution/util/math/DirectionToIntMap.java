package tgw.evolution.util.math;

import net.minecraft.core.Direction;

public class DirectionToIntMap {

    private final int[] values = new int[6];

    public int get(Direction direction) {
        return this.values[direction.get3DDataValue()];
    }

    public int getBeamSize(Direction.Axis axis) {
        int positive = this.get(MathHelper.getPositiveAxis(axis));
        if (positive == 0) {
            return 0;
        }
        int negative = this.get(MathHelper.getNegativeAxis(axis));
        if (negative == 0) {
            return 0;
        }
        return positive + negative;
    }

    public boolean isEmpty() {
        for (int i = 0; i < 6; i++) {
            if (this.values[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public void put(Direction direction, int value) {
        this.values[direction.get3DDataValue()] = value;
    }
}
