package tgw.evolution.client.renderer.chunk;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class EvViewArea {
    protected final Level level;
    protected final EvLevelRenderer levelRenderer;
    public EvChunkRenderDispatcher.RenderChunk[] chunks;
    protected int chunkGridSizeX;
    protected int chunkGridSizeY;
    protected int chunkGridSizeZ;

    public EvViewArea(EvChunkRenderDispatcher dispatcher, Level level, int viewDistance, EvLevelRenderer levelRenderer) {
        this.levelRenderer = levelRenderer;
        this.level = level;
        this.setViewDistance(viewDistance);
        this.createChunks(dispatcher);
    }

    protected void createChunks(EvChunkRenderDispatcher dispatcher) {
        assert Minecraft.getInstance().isSameThread() : "createChunks called from wrong thread: " + Thread.currentThread().getName();
        this.chunks = new EvChunkRenderDispatcher.RenderChunk[this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ];
        for (int x = 0; x < this.chunkGridSizeX; ++x) {
            for (int y = 0; y < this.chunkGridSizeY; ++y) {
                for (int z = 0; z < this.chunkGridSizeZ; ++z) {
                    int index = this.getChunkIndex(x, y, z);
                    //noinspection ObjectAllocationInLoop
                    this.chunks[index] = dispatcher.new RenderChunk(index, x * 16, y * 16, z * 16);
                }
            }
        }
    }

    private int getChunkIndex(int x, int y, int z) {
        return (z * this.chunkGridSizeY + y) * this.chunkGridSizeX + x;
    }

    @Nullable
    public EvChunkRenderDispatcher.RenderChunk getRenderChunkAt(BlockPos pos) {
        return this.getRenderChunkAt(pos.getX(), pos.getY(), pos.getZ());
    }

    @Nullable
    public EvChunkRenderDispatcher.RenderChunk getRenderChunkAt(int posX, int posY, int posZ) {
        int y = posY - this.level.getMinBuildHeight() >> 4;
        if (y >= 0 && y < this.chunkGridSizeY) {
            int x = posX >> 4;
            int z = posZ >> 4;
            x = Mth.positiveModulo(x, this.chunkGridSizeX);
            z = Mth.positiveModulo(z, this.chunkGridSizeZ);
            return this.chunks[this.getChunkIndex(x, y, z)];
        }
        return null;
    }

    public void releaseAllBuffers() {
        for (EvChunkRenderDispatcher.RenderChunk chunk : this.chunks) {
            chunk.releaseBuffers();
        }
    }

    public void repositionCamera(double viewEntityX, double viewEntityZ) {
        int i = Mth.ceil(viewEntityX);
        int j = Mth.ceil(viewEntityZ);
        for (int k = 0; k < this.chunkGridSizeX; ++k) {
            int l = this.chunkGridSizeX * 16;
            int i1 = i - 8 - l / 2;
            int j1 = i1 + Math.floorMod(k * 16 - i1, l);
            for (int k1 = 0; k1 < this.chunkGridSizeZ; ++k1) {
                int l1 = this.chunkGridSizeZ * 16;
                int i2 = j - 8 - l1 / 2;
                int j2 = i2 + Math.floorMod(k1 * 16 - i2, l1);
                for (int k2 = 0; k2 < this.chunkGridSizeY; ++k2) {
                    int l2 = this.level.getMinBuildHeight() + k2 * 16;
                    EvChunkRenderDispatcher.RenderChunk chunk = this.chunks[this.getChunkIndex(k, k2, k1)];
                    if (j1 != chunk.getX() || l2 != chunk.getY() || j2 != chunk.getZ()) {
                        chunk.setOrigin(j1, l2, j2);
                    }
                }
            }
        }
    }

    public void setDirty(int sectionX, int sectionY, int sectionZ, boolean reRenderOnMainThread) {
        int i = Math.floorMod(sectionX, this.chunkGridSizeX);
        int j = Math.floorMod(sectionY - this.level.getMinSection(), this.chunkGridSizeY);
        int k = Math.floorMod(sectionZ, this.chunkGridSizeZ);
        EvChunkRenderDispatcher.RenderChunk chunk = this.chunks[this.getChunkIndex(i, j, k)];
        chunk.setDirty(reRenderOnMainThread);
    }

    protected void setViewDistance(int renderDistanceChunks) {
        int i = renderDistanceChunks * 2 + 1;
        this.chunkGridSizeX = i;
        this.chunkGridSizeY = this.level.getSectionsCount();
        this.chunkGridSizeZ = i;
    }
}
