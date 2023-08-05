package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.network.PacketSCBlockUpdate;
import tgw.evolution.network.PacketSCSectionBlocksUpdate;
import tgw.evolution.patches.PatchChunkHolder;
import tgw.evolution.patches.PatchEither;
import tgw.evolution.util.collection.sets.SHashSet;
import tgw.evolution.util.collection.sets.SSet;
import tgw.evolution.util.math.BlockPosUtil;

import java.util.BitSet;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public abstract class Mixin_M_ChunkHolder implements PatchChunkHolder {

    @Shadow @Final private BitSet blockChangedLightSectionFilter;
    @Shadow @Final private ShortSet[] changedBlocksPerSection;
    @Shadow private boolean hasChangedSections;
    @Shadow @Final private LevelHeightAccessor levelHeightAccessor;
    @Shadow @Final private LevelLightEngine lightEngine;
    @Shadow private boolean resendLight;
    @Shadow @Final private BitSet skyChangedLightSectionFilter;

    @Overwrite
    public void blockChanged(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.blockChanged_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void blockChanged_(int x, int y, int z) {
        LevelChunk levelChunk = this.getTickingChunk();
        //noinspection VariableNotUsedInsideIf
        if (levelChunk != null) {
            int i = this.levelHeightAccessor.getSectionIndex(y);
            if (this.changedBlocksPerSection[i] == null) {
                this.hasChangedSections = true;
                this.changedBlocksPerSection[i] = new SHashSet();
            }
            this.changedBlocksPerSection[i].add(BlockPosUtil.sectionRelativePos(x, y, z));
        }
    }

    @Shadow
    protected abstract void broadcast(Packet<?> packet, boolean bl);

    @Overwrite
    @DeleteMethod
    private void broadcastBlockEntity(Level level, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    private void broadcastBlockEntity(BlockGetter level, int x, int y, int z) {
        BlockEntity tile = level.getBlockEntity_(x, y, z);
        if (tile != null) {
            Packet<?> packet = tile.getUpdatePacket();
            if (packet != null) {
                this.broadcast(packet, false);
            }
        }
    }

    private void broadcastBlockEntityIfNeeded(BlockGetter level, int x, int y, int z, BlockState state) {
        if (state.hasBlockEntity()) {
            this.broadcastBlockEntity(level, x, y, z);
        }
    }

    @Overwrite
    @DeleteMethod
    private void broadcastBlockEntityIfNeeded(Level level, BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Overwrite
    public void broadcastChanges(LevelChunk chunk) {
        if (this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            Level level = chunk.getLevel();
            int changedBlocks = 0;
            for (ShortSet shorts : this.changedBlocksPerSection) {
                changedBlocks += shorts != null ? shorts.size() : 0;
            }
            this.resendLight |= changedBlocks >= 64;
            if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
                this.broadcast(new ClientboundLightUpdatePacket(chunk.getPos(), this.lightEngine, this.skyChangedLightSectionFilter,
                                                                this.blockChangedLightSectionFilter, true), !this.resendLight);
                this.skyChangedLightSectionFilter.clear();
                this.blockChangedLightSectionFilter.clear();
            }
            int secX = chunk.getPos().x;
            int secZ = chunk.getPos().z;
            for (int i = 0, len = this.changedBlocksPerSection.length; i < len; ++i) {
                ShortSet shortSet = this.changedBlocksPerSection[i];
                if (shortSet != null) {
                    int secY = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
                    if (shortSet.size() == 1) {
                        //noinspection ConstantConditions
                        short relative = ((SSet) shortSet).getElement().get();
                        int x = BlockPosUtil.getX(secX, relative);
                        int y = BlockPosUtil.getY(secY, relative);
                        int z = BlockPosUtil.getZ(secZ, relative);
                        BlockState state = level.getBlockState_(x, y, z);
                        //noinspection ObjectAllocationInLoop
                        this.broadcast(new PacketSCBlockUpdate(BlockPos.asLong(x, y, z), state), false);
                        this.broadcastBlockEntityIfNeeded(level, x, y, z, state);
                    }
                    else {
                        LevelChunkSection section = chunk.getSection(i);
                        //noinspection ObjectAllocationInLoop
                        PacketSCSectionBlocksUpdate packet = new PacketSCSectionBlocksUpdate(SectionPos.asLong(secX, secY, secZ),
                                                                                             (SSet) shortSet,
                                                                                             section,
                                                                                             this.resendLight);
                        this.broadcast(packet, false);
                        short[] positions = packet.positions;
                        BlockState[] states = packet.states;
                        int minX = SectionPos.sectionToBlockCoord(secX);
                        int minY = SectionPos.sectionToBlockCoord(secY);
                        int minZ = SectionPos.sectionToBlockCoord(secZ);
                        for (int j = 0; j < positions.length; ++j) {
                            short relative = positions[j];
                            this.broadcastBlockEntityIfNeeded(level,
                                                              minX + SectionPos.sectionRelativeX(relative),
                                                              minY + SectionPos.sectionRelativeY(relative),
                                                              minZ + SectionPos.sectionRelativeZ(relative),
                                                              states[j]);
                        }
                    }
                    this.changedBlocksPerSection[i] = null;
                }
            }
            this.hasChangedSections = false;
        }
    }

    @Overwrite
    public @Nullable LevelChunk getTickingChunk() {
        CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> future = this.getTickingChunkFuture();
        Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = future.getNow(null);
        return either == null ? null : ((PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure>) either).leftOrNull();
    }

    @Shadow
    public abstract CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getTickingChunkFuture();
}
