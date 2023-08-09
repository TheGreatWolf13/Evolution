package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchBlockTintCache;
import tgw.evolution.util.collection.III2IFunction;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ToIntFunction;

@Mixin(BlockTintCache.class)
public abstract class Mixin_CF_BlockTintCache implements PatchBlockTintCache {

    @Mutable @Shadow @Final @RestoreFinal private Long2ObjectLinkedOpenHashMap<BlockTintCache.CacheData> cache;
    @Mutable @Shadow @Final @RestoreFinal private ThreadLocal<BlockTintCache.LatestCacheInfo> latestChunkOnThread;
    @Mutable @Shadow @Final @RestoreFinal private ReentrantReadWriteLock lock;
    @Shadow @Final @DeleteField private ToIntFunction<BlockPos> source;
    @Unique private III2IFunction source_;

    @ModifyConstructor
    public Mixin_CF_BlockTintCache(ToIntFunction<BlockPos> source) {
        this.latestChunkOnThread = ThreadLocal.withInitial(BlockTintCache.LatestCacheInfo::new);
        this.cache = new Long2ObjectLinkedOpenHashMap(256, 0.25F);
        this.lock = new ReentrantReadWriteLock();
    }

    @Shadow
    protected abstract BlockTintCache.CacheData findOrCreateChunkCache(int i, int j);

    @Overwrite
    public int getColor(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getColor_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getColor_(int x, int y, int z) {
        int secX = SectionPos.blockToSectionCoord(x);
        int secZ = SectionPos.blockToSectionCoord(z);
        BlockTintCache.LatestCacheInfo latestCacheInfo = this.latestChunkOnThread.get();
        if (latestCacheInfo.x != secX || latestCacheInfo.z != secZ || latestCacheInfo.cache == null) {
            latestCacheInfo.x = secX;
            latestCacheInfo.z = secZ;
            latestCacheInfo.cache = this.findOrCreateChunkCache(secX, secZ);
        }
        int[] layer = latestCacheInfo.cache.getLayer(y);
        int index = (z & 15) << 4 | x & 15;
        int color = layer[index];
        if (color != 0xffff_ffff) {
            return color;
        }
        color = this.source_.apply(x, y, z);
        layer[index] = color;
        return color;
    }

    @Override
    public void setSource(III2IFunction source) {
        this.source_ = source;
    }
}
