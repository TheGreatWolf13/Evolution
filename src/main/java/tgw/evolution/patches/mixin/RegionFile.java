package tgw.evolution.patches.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.world.level.chunk.storage.RegionBitmap;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class RegionFile implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SECTOR_BYTES = 4_096;
    public static final int SECTOR_INTS = 1_024;
    private static final int CHUNK_HEADER_SIZE = 5;
    private static final ByteBuffer PADDING_BUFFER = ByteBuffer.allocateDirect(1);
    private static final int EXTERNAL_STREAM_FLAG = 128;
    private static final int EXTERNAL_CHUNK_THRESHOLD = 256;
    private final Path externalFileDir;
    private final FileChannel file;
    private final ByteBuffer header;
    private final IntBuffer offsets;
    private final IntBuffer timestamps;
    public final RegionBitmap usedSectors;
    final RegionFileVersion version;

    public RegionFile(Path regionFile, Path externalDir, boolean bl) throws IOException {
        this(regionFile, externalDir, RegionFileVersion.VERSION_DEFLATE, bl);
    }

    public RegionFile(Path regionFile, Path externalDir, RegionFileVersion regionFileVersion, boolean sync) throws IOException {
        this.header = ByteBuffer.allocateDirect(2 * SECTOR_BYTES);
        this.usedSectors = new RegionBitmap();
        this.version = regionFileVersion;
        if (!Files.isDirectory(externalDir)) {
            throw new IllegalArgumentException("Expected directory, got " + externalDir.toAbsolutePath());
        }
        this.externalFileDir = externalDir;
        this.offsets = this.header.asIntBuffer();
        this.offsets.limit(SECTOR_INTS);
        this.header.position(SECTOR_BYTES);
        this.timestamps = this.header.asIntBuffer();
        if (sync) {
            this.file = FileChannel.open(regionFile, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
        }
        else {
            this.file = FileChannel.open(regionFile, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        }
        this.usedSectors.force(0, 2);
        this.header.position(0);
        int i = this.file.read(this.header, 0L);
        if (i != -1) {
            if (i != 2 * SECTOR_BYTES) {
                LOGGER.warn("Region file {} has truncated header: {}", regionFile, i);
            }
            long l = Files.size(regionFile);
            for (int j = 0; j < SECTOR_INTS; ++j) {
                int k = this.offsets.get(j);
                if (k != 0) {
                    int m = getSectorNumber(k);
                    int n = getNumSectors(k);
                    if (m < 2) {
                        LOGGER.warn("Region file {} has invalid sector at index: {}; sector {} overlaps with header", regionFile, j, m);
                        this.offsets.put(j, 0);
                    }
                    else if (n == 0) {
                        LOGGER.warn("Region file {} has an invalid sector at index: {}; size has to be > 0", regionFile, j);
                        this.offsets.put(j, 0);
                    }
                    else if ((long) m * SECTOR_BYTES > l) {
                        LOGGER.warn("Region file {} has an invalid sector at index: {}; sector {} is out of bounds", regionFile, j, m);
                        this.offsets.put(j, 0);
                    }
                    else {
                        this.usedSectors.force(m, n);
                    }
                }
            }
        }

    }

    private static @Nullable DataInputStream createChunkInputStream(int chunkX, int chunkZ, byte version, InputStream inputStream) throws IOException {
        RegionFileVersion regionFileVersion = RegionFileVersion.fromId(version);
        if (regionFileVersion == null) {
            LOGGER.error("Chunk [{}, {}] has invalid chunk stream version {}", chunkX, chunkZ, version);
            return null;
        }
        return new DataInputStream(regionFileVersion.wrap(inputStream));
    }

    private static ByteArrayInputStream createStream(ByteBuffer byteBuffer, int length) {
        return new ByteArrayInputStream(byteBuffer.array(), byteBuffer.position(), length);
    }

    private static byte getExternalChunkVersion(byte version) {
        return (byte) (version & -129);
    }

    private static int getNumSectors(int i) {
        return i & 255;
    }

    private static int getOffsetIndex(int chunkX, int chunkZ) {
        return (chunkX & 31) + (chunkZ & 31) * 32;
    }

    private static int getSectorNumber(int i) {
        return i >> 8 & 0xff_ffff;
    }

    private static int getTimestamp() {
        return (int) (Util.getEpochMillis() / 1_000L);
    }

    private static boolean isExternalStreamChunk(byte b) {
        return (b & EXTERNAL_STREAM_FLAG) != 0;
    }

    private static int packSectorOffset(int i, int j) {
        return i << 8 | j;
    }

    private static int sizeToSectors(int i) {
        return (i + SECTOR_BYTES - 1) / SECTOR_BYTES;
    }

    public void clear(int chunkX, int chunkZ) throws IOException {
        int i = getOffsetIndex(chunkX, chunkZ);
        int j = this.offsets.get(i);
        if (j != 0) {
            this.offsets.put(i, 0);
            this.timestamps.put(i, getTimestamp());
            this.writeHeader();
            Files.deleteIfExists(this.getExternalChunkPath(chunkX, chunkZ));
            this.usedSectors.free(getSectorNumber(j), getNumSectors(j));
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.padToFullSector();
        }
        finally {
            try {
                this.file.force(true);
            }
            finally {
                this.file.close();
            }
        }
    }

    private @Nullable DataInputStream createExternalChunkInputStream(int chunkX, int chunkZ, byte version) throws IOException {
        Path path = this.getExternalChunkPath(chunkX, chunkZ);
        if (!Files.isRegularFile(path)) {
            LOGGER.error("External chunk path {} is not file", path);
            return null;
        }
        return createChunkInputStream(chunkX, chunkZ, version, Files.newInputStream(path));
    }

    private ByteBuffer createExternalStub() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        byteBuffer.putInt(1);
        byteBuffer.put((byte) (this.version.getId() | 128));
        byteBuffer.flip();
        return byteBuffer;
    }

    public boolean doesChunkExist(int chunkX, int chunkZ) {
        int i = this.getOffset(chunkX, chunkZ);
        if (i == 0) {
            return false;
        }
        int j = getSectorNumber(i);
        int k = getNumSectors(i);
        ByteBuffer byteBuffer = ByteBuffer.allocate(CHUNK_HEADER_SIZE);
        try {
            this.file.read(byteBuffer, (long) j * SECTOR_BYTES);
            byteBuffer.flip();
            if (byteBuffer.remaining() != CHUNK_HEADER_SIZE) {
                return false;
            }
            int l = byteBuffer.getInt();
            byte b = byteBuffer.get();
            if (isExternalStreamChunk(b)) {
                if (!RegionFileVersion.isValidVersion(getExternalChunkVersion(b))) {
                    return false;
                }
                return Files.isRegularFile(this.getExternalChunkPath(chunkX, chunkZ));
            }
            if (!RegionFileVersion.isValidVersion(b)) {
                return false;
            }
            if (l == 0) {
                return false;
            }
            int m = l - 1;
            return m >= 0 && m <= SECTOR_BYTES * k;
        }
        catch (IOException e) {
            return false;
        }
    }

    public void flush() throws IOException {
        this.file.force(true);
    }

    public synchronized @Nullable DataInputStream getChunkDataInputStream(int chunkX, int chunkZ) throws IOException {
        int i = this.getOffset(chunkX, chunkZ);
        if (i == 0) {
            return null;
        }
        int j = getSectorNumber(i);
        int k = getNumSectors(i);
        int l = k * SECTOR_BYTES;
        ByteBuffer byteBuffer = ByteBuffer.allocate(l);
        this.file.read(byteBuffer, (long) j * SECTOR_BYTES);
        byteBuffer.flip();
        if (byteBuffer.remaining() < CHUNK_HEADER_SIZE) {
            LOGGER.error("Chunk [{}, {}] header is truncated: expected {} but read {}", chunkX, chunkZ, l, byteBuffer.remaining());
            return null;
        }
        int m = byteBuffer.getInt();
        byte b = byteBuffer.get();
        if (m == 0) {
            LOGGER.warn("Chunk [{}, {}] is allocated, but stream is missing", chunkX, chunkZ);
            return null;
        }
        int n = m - 1;
        if (isExternalStreamChunk(b)) {
            if (n != 0) {
                LOGGER.warn("Chunk has both internal and external streams");
            }
            return this.createExternalChunkInputStream(chunkX, chunkZ, getExternalChunkVersion(b));
        }
        if (n > byteBuffer.remaining()) {
            LOGGER.error("Chunk [{}, {}] stream is truncated: expected {} but read {}", chunkX, chunkZ, n, byteBuffer.remaining());
            return null;
        }
        if (n < 0) {
            LOGGER.error("Declared size {} of chunk [{}, {}] is negative", m, chunkX, chunkZ);
            return null;
        }
        return createChunkInputStream(chunkX, chunkZ, b, createStream(byteBuffer, n));
    }

    public DataOutputStream getChunkDataOutputStream(int chunkX, int chunkZ) throws IOException {
        return new DataOutputStream(this.version.wrap(new ChunkBuffer(chunkX, chunkZ)));
    }

    private Path getExternalChunkPath(int chunkX, int chunkZ) {
        return this.externalFileDir.resolve("c." + chunkX + "." + chunkZ + ".mcc");
    }

    private int getOffset(int chunkX, int chunkZ) {
        return this.offsets.get(getOffsetIndex(chunkX, chunkZ));
    }

    public boolean hasChunk(int chunkX, int chunkZ) {
        return this.getOffset(chunkX, chunkZ) != 0;
    }

    private void padToFullSector() throws IOException {
        int i = (int) this.file.size();
        int j = sizeToSectors(i) * SECTOR_BYTES;
        if (i != j) {
            ByteBuffer byteBuffer = PADDING_BUFFER.duplicate();
            byteBuffer.position(0);
            //noinspection ResultOfMethodCallIgnored
            this.file.write(byteBuffer, j - 1);
        }
    }

    protected synchronized void write(int chunkX, int chunkZ, ByteBuffer byteBuffer) throws IOException {
        int i = getOffsetIndex(chunkX, chunkZ);
        int j = this.offsets.get(i);
        int k = getSectorNumber(j);
        int l = getNumSectors(j);
        int m = byteBuffer.remaining();
        int n = sizeToSectors(m);
        int o;
        CommitOp commitOp;
        if (n >= EXTERNAL_CHUNK_THRESHOLD) {
            Path path = this.getExternalChunkPath(chunkX, chunkZ);
            LOGGER.warn("Saving oversized chunk [{}, {}] ({} bytes} to external file {}", chunkX, chunkZ, m, path);
            n = 1;
            o = this.usedSectors.allocate(n);
            commitOp = this.writeToExternalFile(path, byteBuffer);
            ByteBuffer byteBuffer2 = this.createExternalStub();
            //noinspection ResultOfMethodCallIgnored
            this.file.write(byteBuffer2, (long) o * SECTOR_BYTES);
        }
        else {
            o = this.usedSectors.allocate(n);
            commitOp = () -> Files.deleteIfExists(this.getExternalChunkPath(chunkX, chunkZ));
            //noinspection ResultOfMethodCallIgnored
            this.file.write(byteBuffer, (long) o * SECTOR_BYTES);
        }
        this.offsets.put(i, packSectorOffset(o, n));
        this.timestamps.put(i, getTimestamp());
        this.writeHeader();
        commitOp.run();
        if (k != 0) {
            this.usedSectors.free(k, l);
        }
    }

    private void writeHeader() throws IOException {
        this.header.position(0);
        //noinspection ResultOfMethodCallIgnored
        this.file.write(this.header, 0L);
    }

    private CommitOp writeToExternalFile(Path path, ByteBuffer byteBuffer) throws IOException {
        Path path2 = Files.createTempFile(this.externalFileDir, "tmp", null);
        FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            byteBuffer.position(CHUNK_HEADER_SIZE);
            //noinspection ResultOfMethodCallIgnored
            fileChannel.write(byteBuffer);
        }
        catch (Throwable e) {
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                }
                catch (Throwable var7) {
                    e.addSuppressed(var7);
                }
            }
            throw e;
        }
        fileChannel.close();
        return () -> Files.move(path2, path, StandardCopyOption.REPLACE_EXISTING);
    }

    private class ChunkBuffer extends ByteArrayOutputStream {
        private final int chunkX;
        private final int chunkZ;

        public ChunkBuffer(int chunkX, int chunkZ) {
            super(8_096);
            super.write(0);
            super.write(0);
            super.write(0);
            super.write(0);
            super.write(RegionFile.this.version.getId());
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public void close() throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(this.buf, 0, this.count);
            byteBuffer.putInt(0, this.count - CHUNK_HEADER_SIZE + 1);
            RegionFile.this.write(this.chunkX, this.chunkZ, byteBuffer);
        }
    }

    interface CommitOp {
        void run() throws IOException;
    }
}
