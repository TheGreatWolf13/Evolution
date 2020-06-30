package tgw.evolution.util;

import net.minecraft.util.Direction;

public class DirectionToIntMap {

    private final int[] values = new int[6];

    public void put(Direction direction, int value) {
        this.values[direction.getIndex()] = value;
    }

    public boolean isEmpty() {
        for (int i = 0; i < 6; i++) {
            if (this.values[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public int get(Direction direction) {
        return this.values[direction.getIndex()];
    }

    public boolean containsKey(Direction direction) {
        return this.values[direction.getIndex()] > 0;
    }
}
