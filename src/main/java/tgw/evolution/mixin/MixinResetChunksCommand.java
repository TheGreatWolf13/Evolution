package tgw.evolution.mixin;

import com.mojang.datafixers.util.Unit;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.commands.ResetChunksCommand;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.patches.PatchEither;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.constants.BlockFlags;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Mixin(ResetChunksCommand.class)
public abstract class MixinResetChunksCommand {

    @Unique private static final OList<ChunkStatus> STATUSES;
    @Shadow @Final private static Logger LOGGER;

    static {
        OList<ChunkStatus> list = new OArrayList<>(6);
        list.add(ChunkStatus.BIOMES);
        list.add(ChunkStatus.NOISE);
        list.add(ChunkStatus.SURFACE);
        list.add(ChunkStatus.CARVERS);
        list.add(ChunkStatus.LIQUID_CARVERS);
        list.add(ChunkStatus.FEATURES);
        list.trim();
        STATUSES = list.view();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private static int resetChunks(CommandSourceStack source, int range, boolean skipOldChunks) {
        ServerLevel level = source.getLevel();
        ServerChunkCache chunkSource = level.getChunkSource();
        chunkSource.chunkMap.debugReloadGenerator();
        Vec3 position = source.getPosition();
        int secX0 = SectionPos.blockToSectionCoord(Mth.floor(position.x));
        int secZ0 = SectionPos.blockToSectionCoord(Mth.floor(position.z));
        int minSecZ = secZ0 - range;
        int maxSecZ = secZ0 + range;
        int minSecX = secX0 - range;
        int maxSecX = secX0 + range;
        for (int secZ = minSecZ; secZ <= maxSecZ; ++secZ) {
            for (int secX = minSecX; secX <= maxSecX; ++secX) {
                LevelChunk chunk = chunkSource.getChunk(secX, secZ, false);
                if (chunk != null && (!skipOldChunks || !chunk.isOldNoiseGeneration())) {
                    int x0 = SectionPos.sectionToBlockCoord(secX);
                    int x1 = x0 + 16;
                    int y0 = level.getMinBuildHeight();
                    int y1 = level.getMaxBuildHeight();
                    int z0 = SectionPos.sectionToBlockCoord(secZ);
                    int z1 = z0 + 16;
                    for (int x = x0; x < x1; ++x) {
                        for (int z = z0; z < z1; ++z) {
                            for (int y = y0; y < y1; ++y) {
                                level.setBlock_(x, y, z, Blocks.AIR.defaultBlockState(), BlockFlags.UPDATE_NEIGHBORS);
                            }
                        }
                    }
                }
            }
        }
        ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");
        long startTime = System.currentTimeMillis();
        int totalChunks = (range * 2 + 1) * (range * 2 + 1);
        for (int i = 0, len = STATUSES.size(); i < len; ++i) {
            ChunkStatus status = STATUSES.get(i);
            long statusStart = System.currentTimeMillis();
            Objects.requireNonNull(processorMailbox);
            CompletableFuture<Unit> future = CompletableFuture.supplyAsync(() -> Unit.INSTANCE, processorMailbox::tell);
            for (int secZ = minSecZ; secZ <= maxSecZ; ++secZ) {
                for (int secX = minSecX; secX <= maxSecX; ++secX) {
                    LevelChunk chunk = chunkSource.getChunk(secX, secZ, false);
                    if (chunk != null && (!skipOldChunks || !chunk.isOldNoiseGeneration())) {
                        OList<ChunkAccess> list = new OArrayList<>();
                        int statusRange = Math.max(1, status.getRange());
                        for (int dSecZ = secZ - statusRange; dSecZ <= secZ + statusRange; ++dSecZ) {
                            for (int dSecX = secX - statusRange; dSecX <= secX + statusRange; ++dSecX) {
                                ChunkAccess existingChunk = chunkSource.getChunk(dSecX, dSecZ, status.getParent(), true);
                                if (existingChunk instanceof ImposterProtoChunk imposter) {
                                    list.add(new ImposterProtoChunk(imposter.getWrapped(), true));
                                }
                                else if (existingChunk instanceof LevelChunk c) {
                                    list.add(new ImposterProtoChunk(c, true));
                                }
                                else {
                                    list.add(existingChunk);
                                }
                            }
                        }
                        future = future.thenComposeAsync(unit -> status.generate(processorMailbox::tell,
                                                                                 level,
                                                                                 chunkSource.getGenerator(),
                                                                                 level.getStructureManager(),
                                                                                 chunkSource.getLightEngine(),
                                                                                 chunkAccess -> {
                                                                                     throw new UnsupportedOperationException("Not creating full chunks here");
                                                                                 }, list, true)
                                                                       .thenApply(either -> {
                                                                           if (status == ChunkStatus.NOISE) {
                                                                               PatchEither<ChunkAccess, ChunkHolder.ChunkLoadingFailure> e = (PatchEither) either;
                                                                               if (e.isLeft()) {
                                                                                   ChunkAccess left = e.getLeft();
                                                                                   Heightmap.primeHeightmaps(left, ChunkStatus.POST_FEATURES);
                                                                                   if (left instanceof LevelChunk c) {
//                                                                                       c.primeStructural(true);
                                                                                       c.primeAtm(true);
                                                                                   }
                                                                                   else if (left instanceof ImposterProtoChunk proto) {
                                                                                       LevelChunk c = proto.getWrapped();
//                                                                                       c.primeStructural(true);
                                                                                       c.primeAtm(true);
                                                                                   }
                                                                               }
                                                                           }
                                                                           return Unit.INSTANCE;
                                                                       }), processorMailbox::tell);
                    }
                }
            }
            source.getServer().managedBlock(future::isDone);
            LOGGER.debug(status.getName() + " took " + (System.currentTimeMillis() - statusStart) + " ms");
        }
        long startBlockChanged = System.currentTimeMillis();
        for (int secZ = minSecZ; secZ <= maxSecZ; ++secZ) {
            for (int secX = minSecX; secX <= maxSecX; ++secX) {
                LevelChunk chunk = chunkSource.getChunk(secX, secZ, false);
                if (chunk != null && (!skipOldChunks || !chunk.isOldNoiseGeneration())) {
                    int x0 = SectionPos.sectionToBlockCoord(secX);
                    int x1 = x0 + 16;
                    int y0 = level.getMinBuildHeight();
                    int y1 = level.getMaxBuildHeight();
                    int z0 = SectionPos.sectionToBlockCoord(secZ);
                    int z1 = z0 + 16;
                    for (int x = x0; x < x1; ++x) {
                        for (int z = z0; z < z1; ++z) {
                            for (int y = y0; y < y1; ++y) {
                                chunkSource.blockChanged_(x, y, z);
                            }
                        }
                    }
                }
            }
        }
        LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - startBlockChanged) + " ms");
        long deltaTime = System.currentTimeMillis() - startTime;
        source.sendSuccess(new TextComponent(
                String.format("%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", totalChunks,
                              deltaTime,
                              totalChunks,
                              deltaTime / (float) totalChunks)), true);
        return 1;
    }
}
