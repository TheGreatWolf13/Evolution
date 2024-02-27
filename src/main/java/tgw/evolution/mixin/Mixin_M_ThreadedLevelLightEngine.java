package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.world.lighting.StarLightEngine;
import tgw.evolution.world.lighting.StarLightInterface;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(ThreadedLevelLightEngine.class)
public abstract class Mixin_M_ThreadedLevelLightEngine extends LevelLightEngine implements AutoCloseable {

    @Shadow @Final private static Logger LOGGER;
    @Unique private final Long2IntOpenHashMap chunksBeingWorkedOn = new Long2IntOpenHashMap();
    @Shadow @Final private ChunkMap chunkMap;

    public Mixin_M_ThreadedLevelLightEngine(LightChunkGetter lightChunkGetter, boolean bl, boolean bl2) {
        super(lightChunkGetter, bl, bl2);
    }

    @Override
    @Overwrite
    public void checkBlock(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.checkBlock_(pos.asLong());
    }

    @Override
    public void checkBlock_(long pos) {
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        this.queueTaskForSection(x >> 4, y >> 4, z >> 4, () -> this.getLightEngine().blockChange(pos));
    }

    @Override
    @Overwrite
    public void enableLightSources(ChunkPos chunkPos, boolean bl) {
        Evolution.deprecatedMethod();
        //Do nothing
    }

    @Overwrite
    public CompletableFuture<ChunkAccess> lightChunk(ChunkAccess chunk, boolean lit) {
        ChunkPos chunkPos = chunk.getPos();
        return CompletableFuture.supplyAsync(() -> {
            final Boolean[] emptySections = StarLightEngine.getEmptySectionsForChunk(chunk);
            if (!lit) {
                chunk.setLightCorrect(false);
                this.getLightEngine().lightChunk(chunk, emptySections);
                chunk.setLightCorrect(true);
            }
            else {
                this.getLightEngine().forceLoadInChunk(chunk, emptySections);
                // can't really force the chunk to be edged checked, as we need neighbouring chunks - but we don't have
                // them, so if it's not loaded then I guess we can't do edge checks. Later loads of the chunk should
                // catch what we miss here.
                this.getLightEngine().checkChunkEdges(chunkPos.x, chunkPos.z);
            }
            this.chunkMap.releaseLightTicket(chunkPos);
            return chunk;
        }, runnable -> {
            this.getLightEngine().scheduleChunkLight(chunkPos, runnable);
            this.tryScheduleUpdate();
        }).whenComplete((c, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Failed to light chunk " + chunkPos, throwable);
            }
        });
    }

    @Override
    @Overwrite
    public void onBlockEmissionIncrease(BlockPos pos, int lightEmission) {
        Evolution.deprecatedMethod();
        throw Util.pauseInIde(new UnsupportedOperationException("Ran automatically on a different thread!"));
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void queueSectionData(LightLayer lightLayer, SectionPos sectionPos, @Nullable DataLayer dataLayer, boolean bl) {
        throw new AbstractMethodError();
    }

    @Unique
    private void queueTaskForSection(final int chunkX, final int chunkY, final int chunkZ, final Supplier<CompletableFuture<Void>> runnable) {
        //noinspection TypeMayBeWeakened
        ServerLevel level = (ServerLevel) this.getLightEngine().getLevel();
        ChunkAccess center = this.getLightEngine().getAnyChunkNow(chunkX, chunkZ);
        if (center == null || !center.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
            // do not accept updates in unlit chunks, unless we might be generating a chunk. thanks to the amazing
            // chunk scheduling, we could be lighting and generating a chunk at the same time
            return;
        }
        if (center.getStatus() != ChunkStatus.FULL) {
            // do not keep chunk loaded, we are probably in a gen thread
            // if we proceed to add a ticket the chunk will be loaded, which is not what we want (avoid cascading gen)
            runnable.get();
            return;
        }
        if (!level.getChunkSource().chunkMap.mainThreadExecutor.isSameThread()) {
            // ticket logic is not safe to run off-main, re-schedule
            level.getChunkSource().chunkMap.mainThreadExecutor.execute(() -> this.queueTaskForSection(chunkX, chunkY, chunkZ, runnable));
            return;
        }
        long key = ChunkPos.asLong(chunkX, chunkZ);
        CompletableFuture<Void> updateFuture = runnable.get();
        if (updateFuture == null) {
            // not scheduled
            return;
        }
        int references = this.chunksBeingWorkedOn.addTo(key, 1);
        if (references == 0) {
            ChunkPos pos = new ChunkPos(chunkX, chunkZ);
            level.getChunkSource().addRegionTicket(StarLightInterface.CHUNK_WORK_TICKET, pos, 0, pos);
        }
        // append future to this chunk and 1 radius neighbours chunk save futures
        // this prevents us from saving the level without first waiting for the light engine
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                ChunkHolder neighbour = level.getChunkSource().chunkMap.getUpdatingChunkIfPresent(ChunkPos.asLong(chunkX + dx, chunkZ + dz));
                if (neighbour != null) {
                    neighbour.chunkToSave = neighbour.chunkToSave.thenCombine(updateFuture, (curr, ignore) -> curr);
                }
            }
        }
        updateFuture.thenAcceptAsync(ignore -> {
            int newReferences = this.chunksBeingWorkedOn.get(key);
            if (newReferences == 1) {
                this.chunksBeingWorkedOn.remove(key);
                ChunkPos pos = new ChunkPos(chunkX, chunkZ);
                level.getChunkSource().removeRegionTicket(StarLightInterface.CHUNK_WORK_TICKET, pos, 0, pos);
            }
            else {
                this.chunksBeingWorkedOn.put(key, newReferences - 1);
            }
        }, level.getChunkSource().chunkMap.mainThreadExecutor).whenComplete((ignore, thr) -> {
            if (thr != null) {
                LOGGER.error("Failed to remove ticket level for post chunk task " + new ChunkPos(chunkX, chunkZ), thr);
            }
        });
    }

    @Override
    @Overwrite
    public void retainData(ChunkPos chunkPos, boolean bl) {
        //Do nothing
    }

    @Shadow
    public abstract void tryScheduleUpdate();

    @Overwrite
    public void updateChunkStatus(ChunkPos chunkPos) {
        //Do nothing
    }

    @Override
    @Overwrite
    public void updateSectionStatus(SectionPos secPos, boolean hasOnlyAir) {
        Evolution.deprecatedMethod();
        this.updateSectionStatus_sec(secPos.x(), secPos.y(), secPos.z(), hasOnlyAir);
    }

    @Override
    public void updateSectionStatus_sec(int secX, int secY, int secZ, boolean notReady) {
        this.queueTaskForSection(secX, secY, secZ, () -> this.getLightEngine().sectionChange(secX, secY, secZ, notReady));
    }
}
