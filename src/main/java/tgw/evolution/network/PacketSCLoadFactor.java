package tgw.evolution.network;

import it.unimi.dsi.fastutil.bytes.ByteArrays;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.chunk.IntegrityStorage;
import tgw.evolution.capabilities.chunk.StabilityStorage;

import java.util.Arrays;

public class PacketSCLoadFactor implements Packet<ClientGamePacketListener> {

    private static final byte[] EMPTY = new byte[4_096];
    private static final byte[] FULL = new byte[4_096];
    private static final byte[] FULL_COMPACT = new byte[1];

    static {
        Arrays.fill(FULL, (byte) 255);
    }

    public final Action action;
    public final long pos;
    private final byte @Nullable [] integrity;
    private final byte @Nullable [] loadFactor;
    private final byte @Nullable [] stability;

    public PacketSCLoadFactor() {
        this.action = Action.CLEAR;
        this.pos = 0;
        this.loadFactor = null;
        this.integrity = null;
        this.stability = null;
    }

    public PacketSCLoadFactor(FriendlyByteBuf buf) {
        this.action = Action.VALUES[buf.readVarInt()];
        switch (this.action) {
            case CLEAR -> {
                this.pos = 0;
                this.loadFactor = null;
                this.integrity = null;
                this.stability = null;
            }
            case REMOVE -> {
                this.pos = buf.readLong();
                this.loadFactor = null;
                this.integrity = null;
                this.stability = null;
            }
            case ADD -> {
                this.pos = buf.readLong();
                this.loadFactor = buf.readByteArray();
                this.integrity = buf.readByteArray();
                this.stability = buf.readByteArray();
            }
            default -> throw new IncompatibleClassChangeError();
        }
    }

    public PacketSCLoadFactor(long pos) {
        this.action = Action.REMOVE;
        this.pos = pos;
        this.loadFactor = null;
        this.integrity = null;
        this.stability = null;
    }

    public PacketSCLoadFactor(long pos, IntegrityStorage loadFactorStorage, IntegrityStorage integrityStorage, StabilityStorage stabilityStorage) {
        this.pos = pos;
        this.action = Action.ADD;
        try {
            loadFactorStorage.acquire();
            if (loadFactorStorage.isEmpty()) {
                this.loadFactor = ByteArrays.EMPTY_ARRAY;
            }
            else {
                if (loadFactorStorage.isFull()) {
                    this.loadFactor = FULL_COMPACT;
                }
                else {
                    this.loadFactor = loadFactorStorage.copyBackingArray();
                }
            }
        }
        finally {
            loadFactorStorage.release();
        }
        try {
            integrityStorage.acquire();
            if (integrityStorage.isEmpty()) {
                this.integrity = ByteArrays.EMPTY_ARRAY;
            }
            else {
                if (integrityStorage.isFull()) {
                    this.integrity = FULL_COMPACT;
                }
                else {
                    this.integrity = integrityStorage.copyBackingArray();
                }
            }
        }
        finally {
            integrityStorage.release();
        }
        try {
            stabilityStorage.acquire();
            if (stabilityStorage.isEmpty()) {
                this.stability = ByteArrays.EMPTY_ARRAY;
            }
            else {
                if (stabilityStorage.isFull()) {
                    this.stability = FULL_COMPACT;
                }
                else {
                    this.stability = stabilityStorage.copyBackingArray();
                }
            }
        }
        finally {
            stabilityStorage.release();
        }
    }

    public byte[] getIntegrityArray() {
        assert this.integrity != null;
        if (this.integrity.length == 0) {
            return EMPTY;
        }
        if (this.integrity.length != 4_096) {
            return FULL;
        }
        return this.integrity;
    }

    public byte[] getLoadArray() {
        assert this.loadFactor != null;
        if (this.loadFactor.length == 0) {
            return EMPTY;
        }
        if (this.loadFactor.length != 4_096) {
            return FULL;
        }
        return this.loadFactor;
    }

    public byte[] getStabilityArray() {
        assert this.stability != null;
        if (this.stability.length == 0) {
            return EMPTY;
        }
        if (this.stability.length != 512) {
            return FULL;
        }
        return this.stability;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleLoadFactor(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.action.ordinal());
        switch (this.action) {
            case REMOVE -> buf.writeLong(this.pos);
            case ADD -> {
                assert this.loadFactor != null;
                assert this.integrity != null;
                assert this.stability != null;
                buf.writeLong(this.pos);
                buf.writeByteArray(this.loadFactor);
                buf.writeByteArray(this.integrity);
                buf.writeByteArray(this.stability);
            }
        }
    }

    public enum Action {
        CLEAR,
        ADD,
        REMOVE;

        public static final Action[] VALUES = values();
    }
}
