package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchStorage;
import tgw.evolution.util.physics.EarthHelper;

import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ClientChunkCache.Storage.class)
public abstract class Mixin_CF_ClientChunkCache_Storage implements PatchStorage {

    @Mutable @Shadow @Final @RestoreFinal public int chunkRadius;
    @Mutable @Shadow @Final @RestoreFinal public AtomicReferenceArray<LevelChunk> chunks;
    @Shadow public volatile int viewCenterX;
    @Shadow public volatile int viewCenterZ;
    @Unique final int cacheLength;
    @Unique int cameraChunkCount;
    @Nullable @Unique AtomicReferenceArray<LevelChunk> cameraChunks;
    @Unique volatile int cameraViewCenterX;
    @Unique volatile int cameraViewCenterZ;
    @Shadow int chunkCount;
    @Mutable @Shadow(aliases = "this$0") @Final @RestoreFinal ClientChunkCache field_16254;
    @Mutable @Shadow @Final @RestoreFinal private int viewRange;

    @ModifyConstructor
    Mixin_CF_ClientChunkCache_Storage(ClientChunkCache parent, int i) {
        this.field_16254 = parent;
        this.chunkRadius = i;
        this.viewRange = i * 2 + 1;
        this.cacheLength = Mth.smallestEncompassingPowerOfTwo(this.viewRange);
        this.chunks = new AtomicReferenceArray<>(this.cacheLength * this.cacheLength);
    }

    @Override
    public LevelChunk cameraReplace(int index, LevelChunk oldChunk, @Nullable LevelChunk newChunk) {
        if (this.cameraChunks == null) {
            this.cameraChunks = new AtomicReferenceArray<>(this.cacheLength * this.cacheLength);
        }
        if (this.cameraChunks.compareAndSet(index, oldChunk, newChunk) && newChunk == null) {
            this.chunkCount--;
            this.cameraChunkCount--;
            if (this.cameraChunkCount == 0) {
                this.cameraChunks = null;
            }
        }
        this.field_16254.level.unload(oldChunk);
        return oldChunk;
    }

    @Override
    public void cameraReplace(int index, LevelChunk chunk) {
        if (this.cameraChunks == null) {
            this.cameraChunks = new AtomicReferenceArray<>(this.cacheLength * this.cacheLength);
        }
        LevelChunk oldChunk = this.cameraChunks.getAndSet(index, chunk);
        if (oldChunk != null) {
            this.cameraChunkCount--;
            if (!this.inRange(oldChunk.getPos().x, oldChunk.getPos().z)) {
                this.field_16254.level.unload(oldChunk);
            }
        }
//        if (chunk != null) {
//            this.cameraChunkCount++;
//        }
    }

    @Override
    public int getCamViewCenterX() {
        return this.cameraViewCenterX;
    }

    @Override
    public int getCamViewCenterZ() {
        return this.cameraViewCenterZ;
    }

    @Override
    public @Nullable LevelChunk getCameraChunk(int index) {
        if (this.cameraChunks != null) {
            return this.cameraChunks.get(index);
        }
        return null;
    }

    @Override
    public int getCameraChunksLength() {
        return this.cameraChunks == null ? 0 : this.cameraChunks.length();
    }

    @Override
    public int getCameraIndex(int x, int z) {
        x = EarthHelper.wrapChunkCoordinate(x - this.cameraViewCenterX);
        z = EarthHelper.wrapChunkCoordinate(z - this.cameraViewCenterZ);
        return Math.floorMod(z + this.cameraViewCenterZ, this.cacheLength) * this.cacheLength + Math.floorMod(x + this.cameraViewCenterX, this.cacheLength);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public int getIndex(int x, int z) {
        x = EarthHelper.wrapChunkCoordinate(x);
        z = EarthHelper.wrapChunkCoordinate(z);
        return Math.floorMod(z, this.cacheLength) * this.cacheLength + Math.floorMod(x, this.cacheLength);
    }

    @Override
    public boolean inCameraRange(int x, int z) {
        return EarthHelper.absDeltaChunkCoordinate(x, this.cameraViewCenterX) <= this.chunkRadius && EarthHelper.absDeltaChunkCoordinate(z, this.cameraViewCenterZ) <= this.chunkRadius;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean inRange(int x, int z) {
        return EarthHelper.absDeltaChunkCoordinate(x, this.viewCenterX) <= this.chunkRadius && EarthHelper.absDeltaChunkCoordinate(z, this.viewCenterZ) <= this.chunkRadius;
    }

    /**
     * @author TheGreatWolf
     * @reason Prevent player from unloading camera loaded chunks
     */
    @Overwrite
    public void replace(int index, @Nullable LevelChunk newChunk) {
        LevelChunk oldChunk = this.chunks.getAndSet(index, newChunk);
        if (oldChunk != null) {
            --this.chunkCount;
            if (!this.inCameraRange(oldChunk.getPos().x, oldChunk.getPos().z)) {
                this.field_16254.level.unload(oldChunk);
            }
        }
        //noinspection VariableNotUsedInsideIf
        if (newChunk != null) {
            ++this.chunkCount;
        }
    }

    @Override
    public void setCamViewCenter(int x, int z) {
        this.cameraViewCenterX = x;
        this.cameraViewCenterZ = z;
    }
}
