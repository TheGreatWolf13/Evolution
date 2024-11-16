package tgw.evolution.client.renderer.chunk;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.physics.EarthHelper;

import javax.annotation.Nullable;

public class EvRenderRegionCache {
    
    private final L2OMap<LevelChunk> chunkCache = new L2OHashMap<>();
    private final L2OMap<EvRenderChunk> renderCache = new L2OHashMap<>();
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
        boolean hasAtLeastOneNotEmpty = false;
        for (int x = x0; x <= x1; ++x) {
            int xPrime = EarthHelper.wrapChunkCoordinate(x);
            for (int z = z0; z <= z1; ++z) {
                int zPrime = EarthHelper.wrapChunkCoordinate(z);
                long key = ChunkPos.asLong(xPrime, zPrime);
                LevelChunk chunk = this.chunkCache.get(key);
                if (chunk == null) {
                    chunk = level.getChunk(xPrime, zPrime);
                    this.chunkCache.put(key, chunk);
                }
                if (!hasAtLeastOneNotEmpty && !chunk.isYSpaceEmpty(startY, endY)) {
                    hasAtLeastOneNotEmpty = true;
                }
                this.tempList.add(chunk);
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
