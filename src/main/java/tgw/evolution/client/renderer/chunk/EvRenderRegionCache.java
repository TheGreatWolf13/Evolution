package tgw.evolution.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

import javax.annotation.Nullable;

public class EvRenderRegionCache {
    private final Long2ObjectMap<LevelChunk> chunkCache = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<EvRenderChunk> renderCache = new Long2ObjectOpenHashMap<>();
    private final OList<LevelChunk> tempList = new OArrayList<>();

    public void clear() {
        this.tempList.clear();
        this.renderCache.clear();
        this.chunkCache.clear();
    }

    @Nullable
    public EvRenderChunkRegion createRegion(Level level, int startX, int startY, int startZ, int endX, int endY, int endZ, int offset) {
        int x0 = SectionPos.blockToSectionCoord(startX - offset);
        int z0 = SectionPos.blockToSectionCoord(startZ - offset);
        int x1 = SectionPos.blockToSectionCoord(endX + offset);
        int z1 = SectionPos.blockToSectionCoord(endZ + offset);
        this.tempList.clear();
        for (int x = x0; x <= x1; ++x) {
            for (int z = z0; z <= z1; ++z) {
                long key = ChunkPos.asLong(x, z);
                LevelChunk chunk = this.chunkCache.get(key);
                if (chunk == null) {
                    chunk = level.getChunk(x, z);
                    this.chunkCache.put(key, chunk);
                }
                this.tempList.add(chunk);
            }
        }
        boolean hasAtLeastOneNotEmpty = false;
        for (int i = 0, len = this.tempList.size(); i < len; i++) {
            if (!this.tempList.get(i).isYSpaceEmpty(startY, endY)) {
                hasAtLeastOneNotEmpty = true;
                break;
            }
        }
        if (!hasAtLeastOneNotEmpty) {
            return null;
        }
        EvRenderChunk[][] renderChunks = new EvRenderChunk[x1 - x0 + 1][z1 - z0 + 1];
        int i = 0;
        for (int x = x0; x <= x1; ++x) {
            for (int z = z0; z <= z1; ++z) {
                LevelChunk chunk = this.tempList.get(i++);
                EvRenderChunk renderChunk = this.renderCache.get(chunk.getPos().toLong());
                if (renderChunk == null) {
                    renderChunk = EvRenderChunk.renderChunk(chunk);
                    if (!renderChunk.isEmpty()) {
                        this.renderCache.put(chunk.getPos().toLong(), renderChunk);
                    }
                }
                renderChunks[x - x0][z - z0] = renderChunk;
            }
        }
        return new EvRenderChunkRegion(level, x0, z0, renderChunks);
    }
}
