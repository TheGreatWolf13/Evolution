package tgw.evolution.mixin;

import net.minecraft.server.level.ChunkTracker;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.physics.EarthHelper;

@Mixin(ChunkTracker.class)
public abstract class MixinChunkTracker extends DynamicGraphMinFixedPoint {

    public MixinChunkTracker(int i, int j, int k) {
        super(i, j, k);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void checkNeighborsAfterUpdate(long chunkPos, int i, boolean bl) {
        int chunkX = ChunkPos.getX(chunkPos);
        int chunkZ = ChunkPos.getZ(chunkPos);
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                long pos = ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(chunkX + dx), EarthHelper.wrapChunkCoordinate(chunkZ + dz));
                if (pos != chunkPos) {
                    this.checkNeighbor(chunkPos, pos, i, bl);
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public int getComputedLevel(long chunkPos, long m, int i) {
        int j = i;
        int chunkX = ChunkPos.getX(chunkPos);
        int chunkZ = ChunkPos.getZ(chunkPos);
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                long pos = ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(chunkX + dx), EarthHelper.wrapChunkCoordinate(chunkZ + dz));
                if (pos == chunkPos) {
                    pos = ChunkPos.INVALID_CHUNK_POS;
                }
                if (pos != m) {
                    int r = this.computeLevelFromNeighbor(pos, chunkPos, this.getLevel(pos));
                    if (j > r) {
                        j = r;
                    }
                    if (j == 0) {
                        return j;
                    }
                }
            }
        }
        return j;
    }
}
