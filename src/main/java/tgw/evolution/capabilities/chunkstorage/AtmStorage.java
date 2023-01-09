package tgw.evolution.capabilities.chunkstorage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ThreadingDetector;
import org.jetbrains.annotations.Contract;

public class AtmStorage {

    private final ThreadingDetector threadingDetector = new ThreadingDetector("AtmStorage");
    private volatile int[] data;
    private short nonEmptyCount;

    @Contract("_ -> new")
    public static AtmStorage read(CompoundTag atm) {
        short nonEmptyCount = atm.getShort("NonEmptyCount");
        if (nonEmptyCount == 0) {
            return new AtmStorage();
        }
        AtmStorage a = new AtmStorage();
        a.nonEmptyCount = nonEmptyCount;
        a.data = new int[16 * 16 * 3];
        System.arraycopy(atm.getIntArray("Data"), 0, a.data, 0, 16 * 16 * 3);
        return a;
    }

    public void acquire() {
        this.threadingDetector.checkAndLock();
    }

    public int get(int x, int y, int z) {
        assert 0 <= x && x <= 15 : "X out of bounds: " + x;
        assert 0 <= y && y <= 15 : "Y out of bounds: " + y;
        assert 0 <= z && z <= 15 : "Z out of bounds: " + z;
        this.acquire();
        try {
            if (this.nonEmptyCount == 0) {
                return 0;
            }
            return this.get(z, (16 * x + y) * 3);
        }
        finally {
            this.release();
        }
    }

    private int get(int z, int offset) {
        return switch (z) {
            case 0, 1, 2, 3, 4 -> {
                int prev = this.data[offset];
                int shift = 2 + (4 - z) * 6;
                yield prev >>> shift & 0b11_1111;
            }
            case 5 -> {
                int prev0 = this.data[offset];
                int prev1 = this.data[offset + 1];
                yield (prev0 & 0b11) << 4 | prev1 >> 28 & 0b1111;
            }
            case 6, 7, 8, 9 -> {
                int prev = this.data[offset + 1];
                int shift = 4 + (9 - z) * 6;
                yield prev >>> shift & 0b11_1111;
            }
            case 10 -> {
                int prev1 = this.data[offset + 1];
                int prev2 = this.data[offset + 2];
                yield (prev1 & 0b1111) << 2 | prev2 >> 30 & 0b11;
            }
            case 11, 12, 13, 14, 15 -> {
                int prev = this.data[offset + 2];
                int shift = (15 - z) * 6;
                yield prev >>> shift & 0b11_1111;
            }
            default -> throw new IllegalStateException("Unexpected value: " + z);
        };
    }

    public void release() {
        this.threadingDetector.checkAndUnlock();
    }

    @Contract("-> new")
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        this.acquire();
        try {
            tag.putShort("NonEmptyCount", this.nonEmptyCount);
            if (this.nonEmptyCount != 0) {
                tag.putIntArray("Data", this.data);
            }
            return tag;
        }
        finally {
            this.release();
        }
    }

    public void set(int x, int y, int z, int value) {
        assert 0 <= x && x <= 15 : "X out of bounds: " + x;
        assert 0 <= y && y <= 15 : "Y out of bounds: " + y;
        assert 0 <= z && z <= 15 : "Z out of bounds: " + z;
        assert 0 <= value && value <= 63 : "Value out of bounds: " + value;
        this.acquire();
        try {
            this.set(z, (16 * x + y) * 3, value);
        }
        finally {
            this.release();
        }
    }

    private void set(int z, int offset, int value) {
        value &= 0b11_1111;
        if (value != 0) {
            if (this.nonEmptyCount++ == 0) {
                this.data = new int[16 * 16 * 3];
            }
        }
        switch (z) {
            case 0, 1, 2, 3, 4 -> {
                int prev = this.data[offset];
                int shift = 2 + (4 - z) * 6;
                if ((prev >>> shift & 0b11_1111) != 0) {
                    --this.nonEmptyCount;
                }
                prev &= ~(0b11_1111 << shift);
                prev |= value << shift;
                this.data[offset] = prev;
            }
            case 5 -> {
                int prev0 = this.data[offset];
                int prev1 = this.data[offset + 1];
                if (((prev0 & 0b11) << 4 | prev1 >> 28 & 0b1111) != 0) {
                    --this.nonEmptyCount;
                }
                prev0 &= ~0b11;
                prev1 &= ~(0b1111 << 28);
                prev0 |= value >>> 4 & 0b11;
                prev1 |= (value & 0b1111) << 28;
                this.data[offset] = prev0;
                this.data[offset + 1] = prev1;
            }
            case 6, 7, 8, 9 -> {
                int prev = this.data[offset + 1];
                int shift = 4 + (9 - z) * 6;
                if ((prev >>> shift & 0b11_1111) != 0) {
                    --this.nonEmptyCount;
                }
                prev &= ~(0b11_1111 << shift);
                prev |= value << shift;
                this.data[offset + 1] = prev;
            }
            case 10 -> {
                int prev1 = this.data[offset + 1];
                int prev2 = this.data[offset + 2];
                if (((prev1 & 0b1111) << 2 | prev2 >> 30 & 0b11) != 0) {
                    --this.nonEmptyCount;
                }
                prev1 &= ~0b1111;
                prev2 &= ~(0b11 << 30);
                prev1 |= value >>> 2 & 0b1111;
                prev2 |= (value & 0b11) << 30;
                this.data[offset + 1] = prev1;
                this.data[offset + 2] = prev2;
            }
            case 11, 12, 13, 14, 15 -> {
                int prev = this.data[offset + 2];
                int shift = (15 - z) * 6;
                if ((prev >>> shift & 0b11_1111) != 0) {
                    --this.nonEmptyCount;
                }
                prev &= ~(0b11_1111 << shift);
                prev |= value << shift;
                this.data[offset + 2] = prev;
            }
        }
    }
}

