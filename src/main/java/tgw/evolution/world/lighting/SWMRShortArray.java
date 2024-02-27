package tgw.evolution.world.lighting;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;

public final class SWMRShortArray extends SWMRArray {

    public static final int ARRAY_SIZE = 16 * 16 * 16 * 16 / 8;
    /**
     * this allows us to maintain only 1 short array when we're not updating
     */
    static final ThreadLocal<ArrayDeque<byte[]>> WORKING_BYTES_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final byte[] EMPTY = new byte[0];
    private static final int INIT_STATE_NULL = 0; // null
    private static final int INIT_STATE_UNINIT = 1; // uninitialised
    private static final int INIT_STATE_INIT = 2; // initialised
    private static final int INIT_STATE_HIDDEN = 3; // initialised, but conversion to Vanilla data should be treated as if NULL
    private int stateUpdating;
    private volatile int stateVisible;
    private byte @Nullable [] storageUpdating;
    private volatile byte @Nullable [] storageVisible;
    private boolean updatingDirty; // only returns whether storageUpdating is dirty

    public SWMRShortArray() {
        this(null, false); // lazy init
    }

    public SWMRShortArray(byte[] bytes) {
        this(bytes, false);
    }

    public SWMRShortArray(byte @Nullable [] bytes, boolean isNullShort) {
        if (bytes != null && bytes.length != ARRAY_SIZE) {
            throw new IllegalArgumentException("Data of wrong length: " + bytes.length);
        }
        //noinspection VariableNotUsedInsideIf
        this.stateVisible = this.stateUpdating = bytes == null ? isNullShort ? INIT_STATE_NULL : INIT_STATE_UNINIT : INIT_STATE_INIT;
        this.storageUpdating = this.storageVisible = bytes;
    }

    public SWMRShortArray(byte @Nullable [] bytes, int state) {
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

    public static SWMRShortArray fromVanilla(byte @Nullable [] bytes) {
        if (bytes == null) {
            return new SWMRShortArray();
        }
        return new SWMRShortArray(bytes.clone());
    }

    private static boolean isAllZero(byte[] data) {
        for (int i = 0; i < ARRAY_SIZE; ++i) {
            if (data[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * operation type: updating on src, updating on other
     */
    public void extrudeLower(SWMRShortArray other) {
        //IDK WTF this does
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
    @Override
    public int getUpdating(int index) {
        assert 0 <= index && index < 4_096;
        byte[] updating = this.storageUpdating;
        if (updating == null) {
            return 0;
        }
        int mostSig = updating[index << 1];
        int leastSig = updating[(index << 1) + 1];
        return (mostSig << 8 | leastSig) & 0b1_1111_1_1111_1_1111;
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
        assert 0 <= index && index < 4_096;
        byte[] visible = this.storageVisible;
        if (visible == null) {
            return 0;
        }
        int mostSig = visible[index << 1];
        int leastSig = visible[(index << 1) + 1];
        return (mostSig << 8 | leastSig) & 0b1_1111_1_1111_1_1111;
    }

    /**
     * operation type: updating
     */
    @Override
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
    @Override
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
    @Override
    public void set(int index, int value) {
        assert 0 <= index && index < 4_096;
        if (!this.updatingDirty) {
            this.swapUpdatingAndMarkDirty();
        }
        assert this.storageUpdating != null;
        this.storageUpdating[(index << 1) + 1] = (byte) value;
        this.storageUpdating[index << 1] = (byte) (value >>> 8);
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

    /**
     * operation type: visible
     */
    public byte @Nullable [] toVanillaShort() {
        synchronized (this) {
            return switch (this.stateVisible) {
                case INIT_STATE_HIDDEN, INIT_STATE_NULL -> null;
                case INIT_STATE_UNINIT -> EMPTY;
                case INIT_STATE_INIT -> {
                    assert this.storageVisible != null;
                    //noinspection DataFlowIssue
                    yield this.storageVisible.clone();
                }
                default -> throw new IllegalStateException();
            };
        }
    }

    /**
     * operation type: updating
     */
    @Override
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
                    //noinspection ArrayEquality
                    if (this.storageUpdating != this.storageVisible) {
                        assert this.storageUpdating != null;
                        assert this.storageVisible != null;
                        //noinspection DataFlowIssue
                        System.arraycopy(this.storageUpdating, 0, this.storageVisible, 0, ARRAY_SIZE);
                    }
                }
                //noinspection ArrayEquality
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
