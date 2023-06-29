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
    protected int offsetX;
    protected int offsetZ;

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

    @Nullable
    public EvChunkRenderDispatcher.RenderChunk getRenderChunkAt(BlockPos pos) {
        return this.getRenderChunkAt(pos.getX(), pos.getY(), pos.getZ());
    }

    public void releaseAllBuffers() {
        for (EvChunkRenderDispatcher.RenderChunk chunk : this.chunks) {
            chunk.releaseBuffers();
        }
    }

    public void repositionCamera(double viewEntityX, double viewEntityZ) {
        int camX = Mth.ceil(viewEntityX);
        int camZ = Mth.ceil(viewEntityZ);
        int xSize = this.chunkGridSizeX * 16;
        int zSize = this.chunkGridSizeZ * 16;
        int xOffset = this.offsetX = camX - 8 - xSize / 2;
        int zOffset = this.offsetZ = camZ - 8 - zSize / 2;
        for (int x = 0; x < this.chunkGridSizeX; ++x) {
            int originX = xOffset + Math.floorMod(x * 16 - xOffset, xSize);
            for (int z = 0; z < this.chunkGridSizeZ; ++z) {
                int originZ = zOffset + Math.floorMod(z * 16 - zOffset, zSize);
                for (int y = 0; y < this.chunkGridSizeY; ++y) {
                    int originY = this.level.getMinBuildHeight() + y * 16;
                    EvChunkRenderDispatcher.RenderChunk chunk = this.chunks[this.getChunkIndex(x, y, z)];
                    if (originX != chunk.getX() || originY != chunk.getY() || originZ != chunk.getZ()) {
                        chunk.setOrigin(originX, originY, originZ);
                    }
                }
            }
        }
    }

    public void resetVisibility() {
        for (EvChunkRenderDispatcher.RenderChunk chunk : this.chunks) {
            chunk.visibility = Visibility.OUTSIDE;
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
