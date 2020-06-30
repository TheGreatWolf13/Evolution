package tgw.evolution.util;

import net.minecraft.util.Direction;

import java.util.Random;

public class DirectionList {

    private final Direction[] values = new Direction[6];
    private int current;

    public void add(Direction direction) {
        if (this.current == 6) {
            throw new ArrayIndexOutOfBoundsException("List is full");
        }
        this.values[this.current] = direction;
        this.current++;
    }

    public boolean isEmpty() {
        return this.current == 0;
    }

    public void remove(Direction direction) {
        for (int i = 0; i < this.current; i++) {
            if (this.values[i] == direction) {
                this.current--;
                if (i == 5) {
                    break;
                }
                System.arraycopy(this.values, i + 1, this.values, i, 5 - i);
                break;
            }
        }
    }

    public int size() {
        return this.current;
    }

    public Direction getRandom(Random random) {
        if (this.current == 0) {
            throw new IllegalStateException("List is empty, cannot get a random Direction");
        }
        return this.values[random.nextInt(this.current)];
    }
}
