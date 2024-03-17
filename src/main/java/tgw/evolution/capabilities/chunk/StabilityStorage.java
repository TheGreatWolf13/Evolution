package tgw.evolution.capabilities.chunk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ThreadingDetector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class StabilityStorage {

    private final ThreadingDetector threadingDetector = new ThreadingDetector("Storage");
    private byte @Nullable [] data;
    private boolean hasChanges;
    private short nonEmptyCount;

    @Contract("_ -> new")
    public static StabilityStorage read(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return new StabilityStorage();
        }
        short nonEmptyCount = nbt.getShort("NonEmptyCount");
        if (nonEmptyCount == 0) {
            return new StabilityStorage();
        }
        StabilityStorage a = new StabilityStorage();
        a.nonEmptyCount = nonEmptyCount;
        if (nonEmptyCount == 4_096) {
            return a;
        }
        a.data = new byte[512];
        byte[] data = nbt.getByteArray("Data");
        System.arraycopy(data, 0, a.data, 0, Math.min(512, data.length));
        return a;
    }

    public void acquire() {
        this.threadingDetector.checkAndLock();
    }

    public byte[] copyBackingArray() {
        assert this.data != null;
        return this.data.clone();
    }

    public boolean get(int x, int y, int z) {
        assert 0 <= x && x < 16 : "X out of bounds: " + x;
        assert 0 <= y && y < 16 : "Y out of bounds: " + y;
        assert 0 <= z && z < 16 : "Z out of bounds: " + z;
        this.acquire();
        try {
            if (this.nonEmptyCount == 0) {
                return false;
            }
            if (this.nonEmptyCount == 4_096) {
                return true;
            }
            assert this.data != null;
            return (this.data[(x >> 3) + z * 2 + y * 2 * 16] & 1 << (x & 7)) != 0;
        }
        finally {
            this.release();
        }
    }

    public boolean hadChanges() {
        boolean changed = this.hasChanges;
        this.hasChanges = false;
        return changed;
    }

    public boolean isEmpty() {
        return this.nonEmptyCount == 0;
    }

    public boolean isFull() {
        return this.nonEmptyCount == 4_096;
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
            if (this.nonEmptyCount != 0 && this.nonEmptyCount != 4_096) {
                assert this.data != null;
                tag.putByteArray("Data", this.data);
            }
            return tag;
        }
        finally {
            this.release();
        }
    }

    public void set(int x, int y, int z, boolean value) {
        assert 0 <= x && x < 16 : "X out of bounds: " + x;
        assert 0 <= y && y < 16 : "Y out of bounds: " + y;
        assert 0 <= z && z < 16 : "Z out of bounds: " + z;
        this.acquire();
        try {
            int index = (x >> 3) + z * 2 + y * 2 * 16;
            if (!value) {
                if (this.nonEmptyCount == 0) {
                    return;
                }
                if (this.nonEmptyCount == 4_096) {
                    if (this.data == null) {
                        this.data = new byte[512];
                    }
                    Arrays.fill(this.data, (byte) -1);
                }
                assert this.data != null;
            }
            else {
                if (this.nonEmptyCount == 0) {
                    if (this.data == null) {
                        this.data = new byte[512];
                    }
                }
                else if (this.nonEmptyCount == 4_096) {
                    return;
                }
                else {
                    assert this.data != null;
                }
            }
            int mask = 1 << (x & 7);
            boolean oldValue = (this.data[index] & mask) != 0;
            if (oldValue != value) {
                this.data[index] &= (byte) ~mask;
                if (value) {
                    this.data[index] |= (byte) mask;
                    ++this.nonEmptyCount;
                }
                else {
                    --this.nonEmptyCount;
                }
                this.hasChanges = true;
            }
            assert 0 <= this.nonEmptyCount && this.nonEmptyCount <= 4_096;
        }
        finally {
            this.release();
        }
    }
}
