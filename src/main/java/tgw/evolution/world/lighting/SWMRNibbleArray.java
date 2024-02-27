package tgw.evolution.world.lighting;

import net.minecraft.world.level.chunk.DataLayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * SWMR -> Single Writer Multi Reader Nibble Array
 * <p>
 * Null nibble - nibble does not exist, and should not be written to. Just like vanilla - null
 * nibbles are always 0 - and they are never written to directly. Only initialised/uninitialised
 * nibbles can be written to.
 * <p>
 * Uninitialised nibble - They are all 0, but the backing array isn't initialised.
 * <p>
 * Initialised nibble - Has light data.
 */
public final class SWMRNibbleArray {

    public static final int ARRAY_SIZE = 16 * 16 * 16 / (8 / 4); // blocks / bytes per block
    /**
     * this allows us to maintain only 1 byte array when we're not updating
     */
    static final ThreadLocal<ArrayDeque<byte[]>> WORKING_BYTES_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final int INIT_STATE_NULL = 0; // null
    private static final int INIT_STATE_UNINIT = 1; // uninitialised
    private static final int INIT_STATE_INIT = 2; // initialised
    private static final int INIT_STATE_HIDDEN = 3; // initialised, but conversion to Vanilla data should be treated as if NULL
    private int stateUpdating;
    private volatile int stateVisible;
    private byte @Nullable [] storageUpdating;
    private volatile byte @Nullable [] storageVisible;
    private boolean updatingDirty; // only returns whether storageUpdating is dirty

    public SWMRNibbleArray() {
        this(null, false); // lazy init
    }

    public SWMRNibbleArray(byte[] bytes) {
        this(bytes, false);
    }

    public SWMRNibbleArray(byte @Nullable [] bytes, boolean isNullNibble) {
        if (bytes != null && bytes.length != ARRAY_SIZE) {
            throw new IllegalArgumentException("Data of wrong length: " + bytes.length);
        }
        //noinspection VariableNotUsedInsideIf
        this.stateVisible = this.stateUpdating = bytes == null ? isNullNibble ? INIT_STATE_NULL : INIT_STATE_UNINIT : INIT_STATE_INIT;
        this.storageUpdating = this.storageVisible = bytes;
    }

    public SWMRNibbleArray(byte @Nullable [] bytes, int state) {
        if (bytes != null && bytes.length != ARRAY_SIZE) {
            throw new IllegalArgumentException("Data of wrong length: " + bytes.length);
        }
        if (bytes == null && (state == INIT_STATE_INIT || state == INIT_STATE_HIDDEN)) {
            throw new IllegalArgumentException("Data cannot be null and have state be initialised");
        }
        this.stateUpdating = this.stateVisible = state;
        this.storageUpdating = this.storageVisible = bytes;
    }

    private static byte[] allocateBytes() {
        byte[] inPool = WORKING_BYTES_POOL.get().pollFirst();
        if (inPool != null) {
            return inPool;
        }
        return new byte[ARRAY_SIZE];
    }

    private static void freeBytes(byte[] bytes) {
        WORKING_BYTES_POOL.get().addFirst(bytes);
    }

    public static SWMRNibbleArray fromVanilla(DataLayer nibble) {
        if (nibble == null) {
            return new SWMRNibbleArray(null, true);
        }
        if (nibble.isEmpty()) {
            return new SWMRNibbleArray();
        }
        return new SWMRNibbleArray(nibble.getData().clone()); // make sure we don't write to the parameter later
    }

    private static boolean isAllZero(byte[] data) {
        for (int i = 0; i < ARRAY_SIZE >>> 4; ++i) {
            byte whole = data[i << 4];
            for (int k = 1; k < 1 << 4; ++k) {
                whole |= data[i << 4 | k];
            }
            if (whole != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * operation type: updating on src, updating on other
     */
    public void extrudeLower(SWMRNibbleArray other) {
        if (other.stateUpdating == INIT_STATE_NULL) {
            throw new IllegalArgumentException();
        }
        if (other.storageUpdating == null) {
            this.setUninitialised();
            return;
        }
        byte[] src = other.storageUpdating;
        byte[] into;
        if (this.storageUpdating != null) {
            into = this.storageUpdating;
        }
        else {
            this.storageUpdating = into = allocateBytes();
            this.stateUpdating = INIT_STATE_INIT;
        }
        this.updatingDirty = true;
        int start = 0;
        int end = (15 | 15 << 4) >>> 1;
        /* x | (z << 4) | (y << 8) */
        for (int y = 0; y <= 15; ++y) {
            System.arraycopy(src, start, into, y << 8 - 1, end - start + 1);
        }
    }

    public @Nullable SaveState getSaveState() {
        synchronized (this) {
            int state = this.stateVisible;
            byte[] data = this.storageVisible;
            if (state == INIT_STATE_NULL) {
                return null;
            }
            if (state == INIT_STATE_UNINIT) {
                return new SaveState(null, state);
            }
            assert data != null;
            boolean zero = isAllZero(data);
            if (zero) {
                return state == INIT_STATE_INIT ? new SaveState(null, INIT_STATE_UNINIT) : null;
            }
            return new SaveState(data.clone(), state);
        }
    }

    /**
     * operation type: updating
     */
    public int getUpdating(int x, int y, int z) {
        return this.getUpdating(x & 15 | (z & 15) << 4 | (y & 15) << 8);
    }

    /**
     * operation type: updating
     */
    public int getUpdating(int index) {
        // indices range from 0 -> 4096
        byte[] bytes = this.storageUpdating;
        if (bytes == null) {
            return 0;
        }
        byte value = bytes[index >>> 1];
        // if we are an even index, we want lower 4 bits
        // if we are an odd index, we want upper 4 bits
        return value >>> ((index & 1) << 2) & 0xF;
    }

    /**
     * operation type: visible
     */
    public int getVisible(int x, int y, int z) {
        return this.getVisible(x & 15 | (z & 15) << 4 | (y & 15) << 8);
    }

    /**
     * operation type: visible
     */
    public int getVisible(int index) {
        // indices range from 0 -> 4096
        byte[] visibleBytes = this.storageVisible;
        if (visibleBytes == null) {
            return 0;
        }
        byte value = visibleBytes[index >>> 1];
        // if we are an even index, we want lower 4 bits
        // if we are an odd index, we want upper 4 bits
        return value >>> ((index & 1) << 2) & 0xF;
    }

    /**
     * operation type: updating
     */
    public boolean isDirty() {
        return this.stateUpdating != this.stateVisible || this.updatingDirty;
    }

    /**
     * operation type: updating
     */
    public boolean isHiddenUpdating() {
        return this.stateUpdating == INIT_STATE_HIDDEN;
    }

    /**
     * operation type: updating
     */
    public boolean isHiddenVisible() {
        return this.stateVisible == INIT_STATE_HIDDEN;
    }

    /**
     * operation type: updating
     */
    public boolean isInitialisedUpdating() {
        return this.stateUpdating == INIT_STATE_INIT;
    }

    /**
     * operation type: visible
     */
    public boolean isInitialisedVisible() {
        return this.stateVisible == INIT_STATE_INIT;
    }

    /**
     * operation type: updating
     */
    public boolean isNullNibbleUpdating() {
        return this.stateUpdating == INIT_STATE_NULL;
    }

    /**
     * operation type: visible
     */
    public boolean isNullNibbleVisible() {
        return this.stateVisible == INIT_STATE_NULL;
    }

    /**
     * opeartion type: updating
     */
    public boolean isUninitialisedUpdating() {
        return this.stateUpdating == INIT_STATE_UNINIT;
    }

    /**
     * operation type: visible
     */
    public boolean isUninitialisedVisible() {
        return this.stateVisible == INIT_STATE_UNINIT;
    }

    /**
     * operation type: updating
     */
    public void set(int x, int y, int z, int value) {
        this.set(x & 15 | (z & 15) << 4 | (y & 15) << 8, value);
    }

    /**
     * operation type: updating
     */
    public void set(int index, int value) {
        if (!this.updatingDirty) {
            this.swapUpdatingAndMarkDirty();
        }
        int shift = (index & 1) << 2;
        int i = index >>> 1;
        assert this.storageUpdating != null;
        this.storageUpdating[i] = (byte) (this.storageUpdating[i] & 0xF0 >>> shift | value << shift);
    }

    /**
     * operation type: updating
     */
    public void setFull() {
        if (this.stateUpdating != INIT_STATE_HIDDEN) {
            this.stateUpdating = INIT_STATE_INIT;
        }
        Arrays.fill(this.storageUpdating == null || !this.updatingDirty ? this.storageUpdating = allocateBytes() : this.storageUpdating, (byte) -1);
        this.updatingDirty = true;
    }

    /**
     * operation type: updating
     */
    public void setHidden() {
        if (this.stateUpdating == INIT_STATE_HIDDEN) {
            return;
        }
        if (this.stateUpdating != INIT_STATE_INIT) {
            this.setNull();
        }
        else {
            this.stateUpdating = INIT_STATE_HIDDEN;
        }
    }

    /**
     * operation type: updating
     */
    public void setNonNull() {
        if (this.stateUpdating == INIT_STATE_HIDDEN) {
            this.stateUpdating = INIT_STATE_INIT;
            return;
        }
        if (this.stateUpdating != INIT_STATE_NULL) {
            return;
        }
        this.stateUpdating = INIT_STATE_UNINIT;
    }

    /**
     * operation type: updating
     */
    public void setNull() {
        this.stateUpdating = INIT_STATE_NULL;
        if (this.updatingDirty && this.storageUpdating != null) {
            freeBytes(this.storageUpdating);
        }
        this.storageUpdating = null;
        this.updatingDirty = false;
    }

    /**
     * operation type: updating
     */
    public void setUninitialised() {
        this.stateUpdating = INIT_STATE_UNINIT;
        if (this.storageUpdating != null && this.updatingDirty) {
            freeBytes(this.storageUpdating);
        }
        this.storageUpdating = null;
        this.updatingDirty = false;
    }

    /**
     * operation type: updating
     */
    public void setZero() {
        if (this.stateUpdating != INIT_STATE_HIDDEN) {
            this.stateUpdating = INIT_STATE_INIT;
        }
        Arrays.fill(this.storageUpdating == null || !this.updatingDirty ? this.storageUpdating = allocateBytes() : this.storageUpdating, (byte) 0);
        this.updatingDirty = true;
    }

    /**
     * operation type: updating
     */
    private void swapUpdatingAndMarkDirty() {
        if (this.updatingDirty) {
            return;
        }
        if (this.storageUpdating == null) {
            this.storageUpdating = allocateBytes();
            Arrays.fill(this.storageUpdating, (byte) 0);
        }
        else {
            System.arraycopy(this.storageUpdating, 0, this.storageUpdating = allocateBytes(), 0, ARRAY_SIZE);
        }
        if (this.stateUpdating != INIT_STATE_HIDDEN) {
            this.stateUpdating = INIT_STATE_INIT;
        }
        this.updatingDirty = true;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("State: ");
        switch (this.stateVisible) {
            case INIT_STATE_NULL:
                stringBuilder.append("null");
                break;
            case INIT_STATE_UNINIT:
                stringBuilder.append("uninitialised");
                break;
            case INIT_STATE_INIT:
                stringBuilder.append("initialised");
                break;
            case INIT_STATE_HIDDEN:
                stringBuilder.append("hidden");
                break;
            default:
                stringBuilder.append("unknown");
                break;
        }
        stringBuilder.append("\nData:\n");
        final byte[] data = this.storageVisible;
        if (data != null) {
            for (int i = 0; i < 4_096; ++i) {
                // Copied from NibbleArray#toString
                int level = data[i >>> 1] >>> ((i & 1) << 2) & 0xF;
                stringBuilder.append(Integer.toHexString(level));
                if ((i & 15) == 15) {
                    stringBuilder.append("\n");
                }
                if ((i & 255) == 255) {
                    stringBuilder.append("\n");
                }
            }
        }
        else {
            stringBuilder.append("null");
        }
        return stringBuilder.toString();
    }

    /**
     * operation type: visible
     */
    public @Nullable DataLayer toVanillaNibble() {
        synchronized (this) {
            return switch (this.stateVisible) {
                case INIT_STATE_HIDDEN, INIT_STATE_NULL -> null;
                case INIT_STATE_UNINIT -> new DataLayer();
                case INIT_STATE_INIT -> //noinspection DataFlowIssue
                        new DataLayer(this.storageVisible.clone());
                default -> throw new IllegalStateException();
            };
        }
    }

    /**
     * operation type: updating
     */
    public boolean updateVisible() {
        if (!this.isDirty()) {
            return false;
        }
        synchronized (this) {
            if (this.stateUpdating == INIT_STATE_NULL || this.stateUpdating == INIT_STATE_UNINIT) {
                this.storageVisible = null;
            }
            else {
                if (this.storageVisible == null) {
                    assert this.storageUpdating != null;
                    this.storageVisible = this.storageUpdating.clone();
                }
                else {
                    if (this.storageUpdating != this.storageVisible) {
                        assert this.storageUpdating != null;
                        assert this.storageVisible != null;
                        System.arraycopy(this.storageUpdating, 0, this.storageVisible, 0, ARRAY_SIZE);
                    }
                }
                if (this.storageUpdating != this.storageVisible) {
                    assert this.storageUpdating != null;
                    freeBytes(this.storageUpdating);
                }
                this.storageUpdating = this.storageVisible;
            }
            this.updatingDirty = false;
            this.stateVisible = this.stateUpdating;
        }
        return true;
    }

    public static final class SaveState {

        public final byte @Nullable [] data;
        public final int state;

        public SaveState(byte @Nullable [] data, int state) {
            this.data = data;
            this.state = state;
        }
    }
}