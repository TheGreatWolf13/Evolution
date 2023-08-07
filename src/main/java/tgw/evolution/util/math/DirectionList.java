package tgw.evolution.util.math;

import net.minecraft.core.Direction;

import java.util.random.RandomGenerator;

public final class DirectionList {

    public static final int NULL = -1;
    public static final int HORIZONTAL = 0b100_011_101_010_100;
    public static final int ALL_EXCEPT_UP = 0b100_011_101_010_101;

    private DirectionList() {
    }

    public static int add(int data, Direction direction) {
        int size = size(data);
        if (size == 6) {
            throw new ArrayIndexOutOfBoundsException("List is full");
        }
        data = set(data, size++, direction);
        return setSize(data, size);
    }

    public static Direction get(int data, int index) {
        assert 0 <= index && index < 6 : "Invalid index: " + index;
        assert index < size(data) : "Index >= size: " + index;
        return DirectionUtil.ALL[data >> (index + 1) * 3 & 0b111];
    }

    public static int getLast(int data) {
        int size = size(data);
        if (size == 0) {
            throw new IllegalStateException("List is empty, cannot get last");
        }
        return size - 1;
    }

    public static int getRandom(int data, RandomGenerator random) {
        int size = size(data);
        if (size == 0) {
            throw new IllegalStateException("List is empty, cannot get a random Direction");
        }
        return random.nextInt(size);
    }

    public static boolean isEmpty(int data) {
        return (data & 0b111) == 0;
    }

    public static int remove(int data, int index) {
        int size = size(data);
        if (size == 0) {
            throw new IllegalStateException("List is empty, cannot remove");
        }
        data = setSize(data, --size);
        if (index != size) {
            data = MathHelper.deleteBits(data, (index + 1) * 3, 3);
        }
        return data;
    }

    private static int set(int data, int index, Direction dir) {
        assert 0 <= index && index < 6;
        int offset = (index + 1) * 3;
        data &= ~(0b111 << offset);
        return data | dir.ordinal() << offset;
    }

    private static int setSize(int data, int size) {
        assert 0 <= size && size <= 6;
        data &= ~0b111;
        return data | size;
    }

    private static int size(int data) {
        return data & 0b111;
    }
}
