package tgw.evolution.client.renderer.chunk;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ViewArea {

    public ChunkRenderDispatcher.RenderChunk[] chunks;
    private int height;
    private final Level level;
    private int width;

    public ViewArea(ChunkRenderDispatcher dispatcher, Level level, int viewDistance) {
        this.level = level;
        this.setViewDistance(viewDistance);
        this.createChunks(dispatcher);
    }

    protected void createChunks(ChunkRenderDispatcher dispatcher) {
        assert Minecraft.getInstance().isSameThread() : "createChunks called from wrong thread: " + Thread.currentThread().getName();
        this.chunks = new ChunkRenderDispatcher.RenderChunk[this.width * this.height * this.width];
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                for (int z = 0; z < this.width; ++z) {
                    int index = this.getChunkIndex(x, y, z);
                    //noinspection ObjectAllocationInLoop
                    this.chunks[index] = dispatcher.new RenderChunk(index, x * 16, y * 16, z * 16);
                }
            }
        }
    }

    private int getChunkIndex(int x, int y, int z) {
        return (z * this.height + y) * this.width + x;
    }

    @Nullable
    public ChunkRenderDispatcher.RenderChunk getRenderChunkAt(int posX, int posY, int posZ) {
        int y = posY - this.level.getMinBuildHeight() >> 4;
        if (y >= 0 && y < this.height) {
            return this.chunks[this.getChunkIndex(Mth.positiveModulo(posX >> 4, this.width), y, Mth.positiveModulo(posZ >> 4, this.width))];
        }
        return null;
    }

    @Nullable
    public ChunkRenderDispatcher.RenderChunk getRenderChunkAt(BlockPos pos) {
        return this.getRenderChunkAt(pos.getX(), pos.getY(), pos.getZ());
    }

    public void releaseAllBuffers() {
        for (ChunkRenderDispatcher.RenderChunk chunk : this.chunks) {
            chunk.releaseBuffers();
        }
    }

    public void repositionCamera(double viewEntityX, double viewEntityZ) {
        int camX = Mth.ceil(viewEntityX);
        int camZ = Mth.ceil(viewEntityZ);
        int width = this.width;
        int xSize = width * 16;
        int zSize = width * 16;
        int xOffset = camX - 8 - xSize / 2;
        int zOffset = camZ - 8 - zSize / 2;
        for (int x = 0; x < width; ++x) {
            int originX = xOffset + Math.floorMod(x * 16 - xOffset, xSize);
            for (int z = 0; z < width; ++z) {
                int originZ = zOffset + Math.floorMod(z * 16 - zOffset, zSize);
                for (int y = 0, height = this.height; y < height; ++y) {
                    int originY = this.level.getMinBuildHeight() + y * 16;
                    ChunkRenderDispatcher.RenderChunk chunk = this.chunks[this.getChunkIndex(x, y, z)];
                    if (originX != chunk.getX() || originY != chunk.getY() || originZ != chunk.getZ()) {
                        chunk.setOrigin(originX, originY, originZ);
                    }
                }
            }
        }
    }

    public void resetVisibility() {
        for (ChunkRenderDispatcher.RenderChunk chunk : this.chunks) {
            chunk.visibility = Visibility.OUTSIDE;
        }
    }

    public void setDirty(int sectionX, int sectionY, int sectionZ, boolean reRenderOnMainThread) {
        int i = Math.floorMod(sectionX, this.width);
        int j = Math.floorMod(sectionY - this.level.getMinSection(), this.height);
        int k = Math.floorMod(sectionZ, this.width);
        ChunkRenderDispatcher.RenderChunk chunk = this.chunks[this.getChunkIndex(i, j, k)];
        chunk.setDirty(reRenderOnMainThread);
    }

    protected void setViewDistance(int renderDistance) {
        this.width = Mth.smallestEncompassingPowerOfTwo(renderDistance * 2 + 1);
        this.height = this.level.getSectionsCount();
    }
}
