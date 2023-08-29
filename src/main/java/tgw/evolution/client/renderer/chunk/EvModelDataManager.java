package tgw.evolution.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;

import java.lang.ref.WeakReference;

public final class EvModelDataManager {

    private static final L2OMap<LSet> NEED_MODEL_DATA_REFRESH = new L2OHashMap<>();
    private static final L2OMap<Long2ObjectMap<IModelData>> MODEL_DATA_CACHE = new L2OHashMap<>();
    private static WeakReference<ClientLevel> currentLevel = new WeakReference<>(null);

    private EvModelDataManager() {}

    public static @Nullable IModelData getModelData(ClientLevel level, int x, int y, int z) {
        return getModelData(level, ChunkPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))).get(BlockPos.asLong(x, y, z));
    }

    public static Long2ObjectMap<IModelData> getModelData(ClientLevel level, long chunkPos) {
        refreshModelData(level, chunkPos);
        synchronized (MODEL_DATA_CACHE) {
            return MODEL_DATA_CACHE.getOrDefault(chunkPos, Long2ObjectMaps.emptyMap());
        }
    }

    private static Long2ObjectMap<IModelData> getModelDataCache(long chunkPos) {
        synchronized (MODEL_DATA_CACHE) {
            Long2ObjectMap<IModelData> data = MODEL_DATA_CACHE.get(chunkPos);
            if (data == null) {
                data = Long2ObjectMaps.synchronize(new L2OHashMap<>());
                MODEL_DATA_CACHE.put(chunkPos, data);
            }
            return data;
        }
    }

    public static void onChunkUnload(LevelChunk chunk) {
        if (!chunk.getLevel().isClientSide()) {
            return;
        }
        long chunkPos = chunk.getPos().toLong();
        synchronized (NEED_MODEL_DATA_REFRESH) {
            NEED_MODEL_DATA_REFRESH.remove(chunkPos);
        }
        synchronized (MODEL_DATA_CACHE) {
            MODEL_DATA_CACHE.remove(chunkPos);
        }
    }

    private static void refreshModelData(ClientLevel level, long chunkPos) {
        verifyLevel(level);
        synchronized (NEED_MODEL_DATA_REFRESH) {
            LSet needUpdate = NEED_MODEL_DATA_REFRESH.remove(chunkPos);
            if (needUpdate != null) {
                Long2ObjectMap<IModelData> data = getModelDataCache(chunkPos);
                for (LSet.Entry e = needUpdate.fastEntries(); e != null; e = needUpdate.fastEntries()) {
                    long pos = e.get();
                    BlockEntity toUpdate = level.getBlockEntity_(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos));
                    if (toUpdate != null && !toUpdate.isRemoved()) {
                        data.put(pos, toUpdate.getModelData());
                    }
                    else {
                        data.remove(pos);
                    }
                }
            }
        }
    }

    public static void requestModelDataRefresh(BlockEntity te) {
        Level level = te.getLevel();
        if (level == null || !level.isClientSide) {
            return;
        }
        verifyLevel((ClientLevel) level);
        BlockPos pos = te.getBlockPos();
        long chunkPos = ChunkPos.asLong(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        synchronized (NEED_MODEL_DATA_REFRESH) {
            LSet cache = NEED_MODEL_DATA_REFRESH.get(chunkPos);
            if (cache == null) {
                cache = new LHashSet();
                NEED_MODEL_DATA_REFRESH.put(chunkPos, cache);
            }
            cache.add(pos.asLong());
        }
    }

    private static void verifyLevel(ClientLevel level) {
        assert level == Minecraft.getInstance().level;
        if (level != currentLevel.get()) {
            currentLevel = new WeakReference<>(level);
            synchronized (NEED_MODEL_DATA_REFRESH) {
                NEED_MODEL_DATA_REFRESH.clear();
            }
            synchronized (MODEL_DATA_CACHE) {
                MODEL_DATA_CACHE.clear();
            }
        }
    }
}
