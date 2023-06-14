package tgw.evolution.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fml.common.Mod;
import tgw.evolution.Evolution;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

@Mod.EventBusSubscriber(modid = Evolution.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class EvModelDataManager {

    private static final Long2ObjectMap<LongSet> NEED_MODEL_DATA_REFRESH = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    private static final Long2ObjectMap<Long2ObjectMap<IModelData>> MODEL_DATA_CACHE = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    private static WeakReference<ClientLevel> currentLevel = new WeakReference<>(null);

    private EvModelDataManager() {}

    private static void cleanCaches(ClientLevel level) {
        assert level == Minecraft.getInstance().level;
        if (level != currentLevel.get()) {
            currentLevel = new WeakReference<>(level);
            NEED_MODEL_DATA_REFRESH.clear();
            MODEL_DATA_CACHE.clear();
        }
    }

    public static @Nullable IModelData getModelData(ClientLevel level, int x, int y, int z) {
        return getModelData(level, ChunkPos.asLong(SectionPos.blockToSectionCoord(x),
                                                   SectionPos.blockToSectionCoord(z))).get(BlockPos.asLong(x, y, z));
    }

    public static Long2ObjectMap<IModelData> getModelData(ClientLevel level, long chunkPos) {
        refreshModelData(level, chunkPos);
        return MODEL_DATA_CACHE.getOrDefault(chunkPos, Long2ObjectMaps.emptyMap());
    }

    public static void onChunkUnload(LevelChunk chunk) {
        if (!chunk.getLevel().isClientSide()) {
            return;
        }
        long chunkPos = chunk.getPos().toLong();
        NEED_MODEL_DATA_REFRESH.remove(chunkPos);
        MODEL_DATA_CACHE.remove(chunkPos);
    }

    private static void refreshModelData(ClientLevel level, long chunkPos) {
        cleanCaches(level);
        LongSet needUpdate = NEED_MODEL_DATA_REFRESH.remove(chunkPos);
        if (needUpdate != null) {
            Long2ObjectMap<IModelData> data = MODEL_DATA_CACHE.get(chunkPos);
            if (data == null) {
                data = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
                MODEL_DATA_CACHE.put(chunkPos, data);
            }
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (LongIterator it = needUpdate.longIterator(); it.hasNext(); ) {
                long pos = it.nextLong();
                BlockEntity toUpdate = level.getBlockEntity(mutablePos.set(pos));
                if (toUpdate != null && !toUpdate.isRemoved()) {
                    data.put(pos, toUpdate.getModelData());
                }
                else {
                    data.remove(pos);
                }
            }
        }
    }

    public static void requestModelDataRefresh(BlockEntity te) {
        Level level = te.getLevel();
        if (level == null || !level.isClientSide) {
            return;
        }
        cleanCaches((ClientLevel) level);
        BlockPos pos = te.getBlockPos();
        long chunkPos = ChunkPos.asLong(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        LongSet cache = NEED_MODEL_DATA_REFRESH.get(chunkPos);
        if (cache == null) {
            cache = LongSets.synchronize(new LongOpenHashSet());
            NEED_MODEL_DATA_REFRESH.put(chunkPos, cache);
        }
        cache.add(pos.asLong());
    }
}
