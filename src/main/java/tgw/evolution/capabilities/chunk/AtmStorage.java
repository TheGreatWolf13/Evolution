package tgw.evolution.capabilities.chunk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ThreadingDetector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.Evolution;

import java.util.Arrays;

public class AtmStorage {

    private final ThreadingDetector threadingDetector = new ThreadingDetector("AtmStorage");
    private long @Nullable [] data;
    private short nonEmptyCount;
    private short totallyFullCount;

    @Contract("_ -> new")
    public static AtmStorage read(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return new AtmStorage();
        }
        short nonEmptyCount = nbt.getShort("NonEmptyCount");
        if (nonEmptyCount == 0) {
            return new AtmStorage();
        }
        AtmStorage a = new AtmStorage();
        a.nonEmptyCount = nonEmptyCount;
        a.data = new long[5 * 4_096 / 64];
        short totallyFullCount = nbt.getShort("TotallyFullCount");
        a.totallyFullCount = totallyFullCount;
        if (totallyFullCount == 4_096) {
            Arrays.fill(a.data, -1L);
            return a;
        }
        long[] data = nbt.getLongArray("Data");
        System.arraycopy(data, 0, a.data, 0, Math.min(5 * 4_096 / 64, data.length));
        return a;
    }

    private void acquire() {
        this.threadingDetector.checkAndLock();
    }

    public int get(@Range(from = 0, to = 15) int x, @Range(from = 0, to = 15) int y, @Range(from = 0, to = 15) int z) {
        this.acquire();
        try {
            if (this.nonEmptyCount == 0) {
                return 0;
            }
            if (this.totallyFullCount == 4_096) {
                return 31;
            }
            return this.get(16 * (x & 0b11) + z, 20 * y + 5 * (x >> 2));
        }
        finally {
            this.release();
        }
    }

    private int get(int localZ, int offset) {
        assert this.data != null;
        return switch (localZ) {
            case 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 -> {
                long prev = this.data[offset];
                int shift = 4 + (11 - localZ) * 5;
                yield (int) (prev >>> shift & 0b1_1111);
            }
            case 12 -> {
                //4 bits from prev0, 1 bit from prev1
                long prev0 = this.data[offset];
                long prev1 = this.data[offset + 1];
                yield (int) ((prev0 & 0b1111) << 1 | prev1 >> 63 & 0b1);
            }
            case 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 -> {
                long prev = this.data[offset + 1];
                int shift = 3 + (24 - localZ) * 5;
                yield (int) (prev >>> shift & 0b1_1111);
            }
            case 25 -> {
                //3 bits from prev1, 2 bits from prev2
                long prev1 = this.data[offset + 1];
                long prev2 = this.data[offset + 2];
                yield (int) ((prev1 & 0b111) << 2 | prev2 >> 62 & 0b11);
            }
            case 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37 -> {
                long prev = this.data[offset + 2];
                int shift = 2 + (37 - localZ) * 5;
                yield (int) (prev >>> shift & 0b1_1111);
            }
            case 38 -> {
                //2 bits from prev2, 3 bits from prev3
                long prev2 = this.data[offset + 2];
                long prev3 = this.data[offset + 3];
                yield (int) ((prev2 & 0b11) << 3 | prev3 >> 61 & 0b111);
            }
            case 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50 -> {
                long prev = this.data[offset + 3];
                int shift = 1 + (50 - localZ) * 5;
                yield (int) (prev >>> shift & 0b1_1111);
            }
            case 51 -> {
                //1 bit from prev3, 4 bits from prev4
                long prev3 = this.data[offset + 3];
                long prev4 = this.data[offset + 4];
                yield (int) ((prev3 & 0b1) << 4 | prev4 >> 60 & 0b1111);
            }
            case 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63 -> {
                long prev = this.data[offset + 4];
                int shift = (63 - localZ) * 5;
                yield (int) (prev >>> shift & 0b1_1111);
            }
            default -> throw new IllegalStateException("Unexpected value: " + localZ);
        };
    }

    private void release() {
        this.threadingDetector.checkAndUnlock();
    }

    public void reset() {
        this.acquire();
        try {
            if (this.data != null) {
                Arrays.fill(this.data, 0);
            }
            this.nonEmptyCount = 0;
            this.totallyFullCount = 0;
        }
        finally {
            this.release();
        }
    }

    @Contract("-> new")
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        this.acquire();
        try {
            tag.putShort("NonEmptyCount", this.nonEmptyCount);
            if (this.nonEmptyCount != 0) {
                tag.putShort("TotallyFullCount", this.totallyFullCount);
                if (this.totallyFullCount != 4_096) {
                    assert this.data != null;
                    tag.putLongArray("Data", this.data);
                }
            }
            return tag;
        }
        finally {
            this.release();
        }
    }

    public void set(@Range(from = 0, to = 15) int x, @Range(from = 0, to = 15) int y, @Range(from = 0, to = 15) int z, @Range(from = 0, to = 31) int value) {
        this.acquire();
        try {
            this.set(16 * (x & 0b11) + z, 20 * y + 5 * (x >> 2), value);
        }
        catch (Exception e) {
            Evolution.error("Atm Storage encountered an error at {}, {}, {}; value = {}", x, y, z, value);
            Evolution.error(e.getMessage());
        }
        finally {
            this.release();
        }
    }

    private void set(int localZ, int offset, int value) {
        assert 0 <= value && value <= 31 : "Value out of bounds: " + value;
        if (value == 0) {
            if (this.nonEmptyCount == 0) {
                return;
            }
            if (this.totallyFullCount == 4_096) {
                if (this.data == null) {
                    this.data = new long[5 * 4_096 / 64];
                }
                Arrays.fill(this.data, -1L);
            }
            assert this.data != null;
        }
        else {
            if (this.nonEmptyCount == 0) {
                if (this.data == null) {
                    this.data = new long[5 * 4_096 / 64];
                }
            }
            else if (this.totallyFullCount == 4_096) {
                if (value == 31) {
                    return;
                }
                if (this.data == null) {
                    this.data = new long[5 * 4_096 / 64];
                }
                Arrays.fill(this.data, -1L);
            }
            else {
                assert this.data != null;
            }
        }
        int old = 0;
        switch (localZ) {
            case 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 -> {
                long prev = this.data[offset];
                int shift = 4 + (11 - localZ) * 5;
                old = (int) (prev >>> shift & 0b1_1111);
                prev &= ~(0b1_1111L << shift);
                prev |= (long) value << shift;
                this.data[offset] = prev;
            }
            case 12 -> {
                //4 bits from prev0, 1 bit from prev1
                long prev0 = this.data[offset];
                long prev1 = this.data[offset + 1];
                old = (int) ((prev0 & 0b1111) << 1 | prev1 >> 63 & 0b1);
                prev0 &= ~0b1111;
                prev1 &= ~(0b1L << 63);
                prev0 |= value >>> 1 & 0b1111;
                prev1 |= (value & 0b1L) << 63;
                this.data[offset] = prev0;
                this.data[offset + 1] = prev1;
            }
            case 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 -> {
                long prev = this.data[offset + 1];
                int shift = 3 + (24 - localZ) * 5;
                old = (int) (prev >>> shift & 0b1_1111);
                prev &= ~(0b1_1111L << shift);
                prev |= (long) value << shift;
                this.data[offset + 1] = prev;
            }
            case 25 -> {
                //3 bits from prev1, 2 bits from prev2
                long prev1 = this.data[offset + 1];
                long prev2 = this.data[offset + 2];
                old = (int) ((prev1 & 0b111) << 2 | prev2 >> 62 & 0b11);
                prev1 &= ~0b111;
                prev2 &= ~(0b11L << 62);
                prev1 |= value >>> 2 & 0b111;
                prev2 |= (value & 0b11L) << 62;
                this.data[offset + 1] = prev1;
                this.data[offset + 2] = prev2;
            }
            case 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37 -> {
                long prev = this.data[offset + 2];
                int shift = 2 + (37 - localZ) * 5;
                old = (int) (prev >>> shift & 0b1_1111);
                prev &= ~(0b1_1111L << shift);
                prev |= (long) value << shift;
                this.data[offset + 2] = prev;
            }
            case 38 -> {
                //2 bits from prev2, 3 bits from prev3
                long prev2 = this.data[offset + 2];
                long prev3 = this.data[offset + 3];
                old = (int) ((prev2 & 0b11) << 3 | prev3 >> 61 & 0b111);
                prev2 &= ~0b11;
                prev3 &= ~(0b111L << 61);
                prev2 |= value >>> 3 & 0b11;
                prev3 |= (value & 0b111L) << 61;
                this.data[offset + 2] = prev2;
                this.data[offset + 3] = prev3;
            }
            case 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50 -> {
                long prev = this.data[offset + 3];
                int shift = 1 + (50 - localZ) * 5;
                old = (int) (prev >>> shift & 0b1_1111);
                prev &= ~(0b1_1111L << shift);
                prev |= (long) value << shift;
                this.data[offset + 3] = prev;
            }
            case 51 -> {
                //1 bit from prev3, 4 bits from prev4
                long prev3 = this.data[offset + 3];
                long prev4 = this.data[offset + 4];
                old = (int) ((prev3 & 0b1) << 4 | prev4 >> 60 & 0b1111);
                prev3 &= ~0b1;
                prev4 &= ~(0b1111L << 60);
                prev3 |= value >>> 4 & 0b1;
                prev4 |= (value & 0b1111L) << 60;
                this.data[offset + 3] = prev3;
                this.data[offset + 4] = prev4;
            }
            case 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63 -> {
                long prev = this.data[offset + 4];
                int shift = (63 - localZ) * 5;
                old = (int) (prev >>> shift & 0b1_1111);
                prev &= ~(0b1_1111L << shift);
                prev |= (long) value << shift;
                this.data[offset + 4] = prev;
            }
        }
        if (old == 0) {
            if (value != 0) {
                if (value == 31) {
                    ++this.totallyFullCount;
                }
                ++this.nonEmptyCount;
            }
        }
        else {
            if (value == 0) {
                --this.nonEmptyCount;
                if (old == 31) {
                    --this.totallyFullCount;
                }
            }
            else if (value != 31) {
                if (old == 31) {
                    --this.totallyFullCount;
                }
            }
            else {
                if (old != 31) {
                    ++this.totallyFullCount;
                }
            }
        }
        assert 0 <= this.nonEmptyCount && this.nonEmptyCount <= 4_096;
        assert 0 <= this.totallyFullCount && this.totallyFullCount <= 4_096;
    }
}

