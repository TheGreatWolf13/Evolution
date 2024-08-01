package tgw.evolution.capabilities.chunk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ThreadingDetector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class IntegrityStorage {

    private byte @Nullable [] data;
    private boolean hasChanges;
    private short nonEmptyCount;
    private final ThreadingDetector threadingDetector = new ThreadingDetector("Storage");
    private short totallyFullCount;

    @Contract("_ -> new")
    public static IntegrityStorage read(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return new IntegrityStorage();
        }
        short nonEmptyCount = nbt.getShort("NonEmptyCount");
        if (nonEmptyCount == 0) {
            return new IntegrityStorage();
        }
        IntegrityStorage a = new IntegrityStorage();
        a.nonEmptyCount = nonEmptyCount;
        a.data = new byte[4_096];
        short totallyFullCount = nbt.getShort("TotallyFullCount");
        a.totallyFullCount = totallyFullCount;
        if (totallyFullCount == 4_096) {
            Arrays.fill(a.data, (byte) -1);
            return a;
        }
        byte[] data = nbt.getByteArray("Data");
        System.arraycopy(data, 0, a.data, 0, Math.min(4_096, data.length));
        return a;
    }

    public void acquire() {
        this.threadingDetector.checkAndLock();
    }

    public byte[] copyBackingArray() {
        assert this.data != null;
        return this.data.clone();
    }

    public int get(int x, int y, int z) {
        assert 0 <= x && x < 16 : "X out of bounds: " + x;
        assert 0 <= y && y < 16 : "Y out of bounds: " + y;
        assert 0 <= z && z < 16 : "Z out of bounds: " + z;
        this.acquire();
        try {
            if (this.nonEmptyCount == 0) {
                return 0;
            }
            if (this.totallyFullCount == 4_096) {
                return 255;
            }
            assert this.data != null;
            return Byte.toUnsignedInt(this.data[x + z * 16 + y * 16 * 16]);
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
        return this.totallyFullCount == 4_096;
    }

    public void release() {
        this.threadingDetector.checkAndUnlock();
    }

    public void reset() {
        this.acquire();
        try {
            if (this.data != null) {
                Arrays.fill(this.data, (byte) 0);
            }
            this.nonEmptyCount = 0;
            this.totallyFullCount = 0;
            this.hasChanges = true;
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
                    tag.putByteArray("Data", this.data);
                }
            }
            return tag;
        }
        finally {
            this.release();
        }
    }

    public void set(int x, int y, int z, int value) {
        assert 0 <= x && x < 16 : "X out of bounds: " + x;
        assert 0 <= y && y < 16 : "Y out of bounds: " + y;
        assert 0 <= z && z < 16 : "Z out of bounds: " + z;
        assert 0 <= value && value < 256 : "Value out of bounds: " + value;
        this.acquire();
        try {
            int index = x + z * 16 + y * 16 * 16;
            if (value == 0) {
                if (this.nonEmptyCount == 0) {
                    return;
                }
                if (this.totallyFullCount == 4_096) {
                    if (this.data == null) {
                        this.data = new byte[4_096];
                    }
                    Arrays.fill(this.data, (byte) -1);
                }
                assert this.data != null;
            }
            else {
                if (this.nonEmptyCount == 0) {
                    if (this.data == null) {
                        this.data = new byte[4_096];
                    }
                }
                else if (this.totallyFullCount == 4_096) {
                    if (value == 255) {
                        return;
                    }
                    if (this.data == null) {
                        this.data = new byte[4_096];
                    }
                    Arrays.fill(this.data, (byte) -1);
                }
                else {
                    assert this.data != null;
                }
            }
            int oldValue = Byte.toUnsignedInt(this.data[index]);
            if (oldValue != value) {
                this.data[index] = (byte) value;
                if (oldValue == 0) {
                    if (value == 255) {
                        ++this.totallyFullCount;
                    }
                    ++this.nonEmptyCount;
                }
                else {
                    if (value == 0) {
                        --this.nonEmptyCount;
                        if (oldValue == 255) {
                            --this.totallyFullCount;
                        }
                    }
                    else if (value != 255) {
                        if (oldValue == 255) {
                            --this.totallyFullCount;
                        }
                    }
                    else {
                        ++this.totallyFullCount;
                    }
                }
                this.hasChanges = true;
            }
            assert 0 <= this.nonEmptyCount && this.nonEmptyCount <= 4_096;
            assert 0 <= this.totallyFullCount && this.totallyFullCount <= 4_096;
        }
        finally {
            this.release();
        }
    }
}
