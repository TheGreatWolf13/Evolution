package tgw.evolution.util;

import net.minecraft.util.Direction;

import java.util.Random;

public class DirectionDiagonalList {

    private final DirectionDiagonal[] values = new DirectionDiagonal[4];
    private int current;

    public void add(DirectionDiagonal direction) {
        for (int i = 0; i < this.current; i++) {
            if (this.values[i] == direction) {
                return;
            }
        }
        this.values[this.current] = direction;
        this.current++;
    }

    public void addFromDirection(Direction direction) {
        switch (direction) {
            case NORTH:
                this.add(DirectionDiagonal.NORTH_WEST);
                this.add(DirectionDiagonal.NORTH_EAST);
                break;
            case SOUTH:
                this.add(DirectionDiagonal.SOUTH_WEST);
                this.add(DirectionDiagonal.SOUTH_EAST);
                break;
            case WEST:
                this.add(DirectionDiagonal.NORTH_WEST);
                this.add(DirectionDiagonal.SOUTH_WEST);
                break;
            case EAST:
                this.add(DirectionDiagonal.NORTH_EAST);
                this.add(DirectionDiagonal.SOUTH_EAST);
                break;
            default:
                throw new IllegalArgumentException("Only directions on the XZ plane are supported for this operation!");
        }
    }

    public void clear() {
        this.current = 0;
    }

    public DirectionDiagonal getRandomAndRemove(Random random) {
        if (this.current == 0) {
            throw new IllegalStateException("List is empty, cannot get a random Direction");
        }
        int index = random.nextInt(this.current);
        DirectionDiagonal diagonal = this.values[index];
        this.current--;
        if (index != 3) {
            System.arraycopy(this.values, index + 1, this.values, index, 3 - index);
        }
        return diagonal;
    }

    public boolean isEmpty() {
        return this.current == 0;
    }

    public void remove(DirectionDiagonal direction) {
        for (int i = 0; i < this.current; i++) {
            if (this.values[i] == direction) {
                this.current--;
                if (i == 3) {
                    break;
                }
                System.arraycopy(this.values, i + 1, this.values, i, 3 - i);
                break;
            }
        }
    }

    public int size() {
        return this.current;
    }
}
