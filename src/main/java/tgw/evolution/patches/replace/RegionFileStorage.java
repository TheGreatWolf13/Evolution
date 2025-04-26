package tgw.evolution.patches.replace;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.util.ExceptionCollector;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.maps.L2OLinkedHashMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RegionFileStorage implements AutoCloseable {
    private static final int MAX_CACHE_SIZE = 256;
    private final Path folder;
    private final L2OLinkedHashMap<RegionFile> regionCache = new L2OLinkedHashMap<>();
    private final boolean sync;

    public RegionFileStorage(Path folder, boolean sync) {
        this.folder = folder;
        this.sync = sync;
    }

    @Override
    public void close() throws IOException {
        ExceptionCollector<IOException> collector = new ExceptionCollector<>();
        L2OLinkedHashMap<RegionFile> regionCache = this.regionCache;
        for (long it = regionCache.beginIteration(); regionCache.hasNextIteration(it); it = regionCache.nextEntry(it)) {
            try {
                regionCache.getIterationValue(it).close();
            }
            catch (IOException e) {
                collector.add(e);
            }
        }
        collector.throwIfPresent();
    }

    public void flush() throws IOException {
        L2OLinkedHashMap<RegionFile> regionCache = this.regionCache;
        for (long it = regionCache.beginIteration(); regionCache.hasNextIteration(it); it = regionCache.nextEntry(it)) {
            regionCache.getIterationValue(it).flush();
        }
    }

    private RegionFile getRegionFile(int chunkX, int chunkZ) throws IOException {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        long regionStartChunkPos = ChunkPos.asLong(regionX, regionZ);
        RegionFile regionFile = this.regionCache.getAndMoveToFirst(regionStartChunkPos);
        if (regionFile != null) {
            return regionFile;
        }
        if (this.regionCache.size() >= MAX_CACHE_SIZE) {
            this.regionCache.removeLast().close();
        }
        Files.createDirectories(this.folder);
        regionFile = new RegionFile(this.folder.resolve("r." + regionX + "." + regionZ + ".mca"), this.folder, this.sync);
        this.regionCache.putAndMoveToFirst(regionStartChunkPos, regionFile);
        return regionFile;
    }

    public @Nullable CompoundTag read(int chunkX, int chunkZ) throws IOException {
        RegionFile regionFile = this.getRegionFile(chunkX, chunkZ);
        DataInputStream inputStream = regionFile.getChunkDataInputStream(chunkX, chunkZ);
        if (inputStream == null) {
            return null;
        }
        CompoundTag tag;
        try {
            tag = NbtIo.read(inputStream);
        }
        catch (Throwable e) {
            try {
                inputStream.close();
            }
            catch (Throwable suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }
        inputStream.close();
        return tag;
    }

    public void scanChunk(int chunkX, int chunkZ, StreamTagVisitor visitor) throws IOException {
        RegionFile regionFile = this.getRegionFile(chunkX, chunkZ);
        DataInputStream inputStream = regionFile.getChunkDataInputStream(chunkX, chunkZ);
        if (inputStream == null) {
            return;
        }
        try {
            NbtIo.parse(inputStream, visitor);
        }
        catch (Throwable e) {
            try {
                inputStream.close();
            }
            catch (Throwable suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }
        inputStream.close();
    }

    public void write(int chunkX, int chunkZ, @Nullable CompoundTag compoundTag) throws IOException {
        RegionFile regionFile = this.getRegionFile(chunkX, chunkZ);
        if (compoundTag == null) {
            regionFile.clear(chunkX, chunkZ);
        }
        else {
            DataOutputStream outputStream = regionFile.getChunkDataOutputStream(chunkX, chunkZ);
            try {
                NbtIo.write(compoundTag, outputStream);
            }
            catch (Throwable e) {
                try {
                    outputStream.close();
                }
                catch (Throwable suppressed) {
                    e.addSuppressed(suppressed);
                }
                throw e;
            }
            outputStream.close();
        }
    }
}
