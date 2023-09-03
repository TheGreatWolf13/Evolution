package tgw.evolution.mixin;

import net.minecraft.util.BitStorage;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.ArrayHelper;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.SimpleEnumSet;

import java.util.Set;
import java.util.function.Predicate;

@Mixin(Heightmap.class)
public abstract class MixinHeightmap {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public Predicate<BlockState> isOpaque;
    @Shadow @Final private ChunkAccess chunk;
    @Shadow @Final private BitStorage data;

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos versions
     */
    @Overwrite
    public static void primeHeightmaps(ChunkAccess chunk, Set<Heightmap.Types> set) {
        OList<Heightmap> list = new OArrayList<>(set.size());
        int y1 = chunk.getHighestSectionPosition() + 16;
        int y0 = chunk.getMinBuildHeight();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                for (Heightmap.Types type : ArrayHelper.HEIGHTMAP) {
                    if (set.contains(type)) {
                        list.add(chunk.getOrCreateHeightmapUnprimed(type));
                    }
                }
                for (int y = y1 - 1; y >= y0; --y) {
                    BlockState state = chunk.getBlockState_(x, y, z);
                    if (!state.is(Blocks.AIR)) {
                        for (int i = 0; i < list.size(); ++i) {
                            Heightmap heightmap = list.get(i);
                            if (heightmap.isOpaque.test(state)) {
                                heightmap.setHeight(x, z, y + 1);
                                list.remove(i--);
                            }
                        }
                        if (list.isEmpty()) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Shadow
    public abstract int getFirstAvailable(int i, int j);

    @Shadow
    public abstract void setHeight(int i, int j, int k);

    /**
     * @author TheGreatWolf
     * @reason Use new map
     */
    @Overwrite
    public void setRawData(ChunkAccess chunkAccess, Heightmap.Types types, long[] ls) {
        long[] ms = this.data.getRaw();
        if (ms.length == ls.length) {
            System.arraycopy(ls, 0, ms, 0, ls.length);
        }
        else {
            LOGGER.warn("Ignoring heightmap data for chunk " +
                        chunkAccess.getPos() +
                        ", size does not match; expected: " +
                        ms.length +
                        ", got: " +
                        ls.length);
            primeHeightmaps(chunkAccess, SimpleEnumSet.of(ArrayHelper.HEIGHTMAP, types));
        }
    }

    @Overwrite
    public boolean update(int localX, int y, int localZ, BlockState state) {
        int currentY = this.getFirstAvailable(localX, localZ);
        if (y <= currentY - 2) {
            return false;
        }
        if (this.isOpaque.test(state)) {
            if (y >= currentY) {
                this.setHeight(localX, localZ, y + 1);
                return true;
            }
        }
        else if (currentY - 1 == y) {
            for (int dy = y - 1; dy >= this.chunk.getMinBuildHeight(); --dy) {
                if (this.isOpaque.test(this.chunk.getBlockState_(localX, dy, localZ))) {
                    this.setHeight(localX, localZ, dy + 1);
                    return true;
                }
            }
            this.setHeight(localX, localZ, this.chunk.getMinBuildHeight());
            return true;
        }
        return false;
    }
}
