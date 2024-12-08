package tgw.evolution.client.renderer.chunk;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.maps.L2OMap;

import java.util.List;

public final class RenderChunk {

    private static final RenderChunk DEBUG = new RenderChunk(false, true);
    private static final RenderChunk EMPTY = new RenderChunk(true, false);
    private final @Nullable L2OMap<BlockEntity> blockEntities;
    /**
     * Bit 0: Empty;<br>
     * Bit 1: Debug;<br>
     */
    private final byte flags;
    private final @Nullable List<PalettedContainer<BlockState>> sections;
    private final @Nullable LevelChunk wrapped;

    private RenderChunk(boolean empty, boolean debug) {
        if (empty) {
            this.flags = 1;
        }
        else {
            assert debug;
            this.flags = 2;
        }
        this.blockEntities = null;
        this.sections = null;
        this.wrapped = null;
    }

    private RenderChunk(LevelChunk wrapped) {
        this.wrapped = wrapped;
        this.flags = 0;
        this.blockEntities = wrapped.blockEntities_();
        LevelChunkSection[] sections = wrapped.getSections();
        this.sections = new OArrayList<>(sections.length);
        for (LevelChunkSection section : sections) {
            this.sections.add(section.hasOnlyAir() ? null : section.getStates().copy());
        }
    }

    public static RenderChunk renderChunk(LevelChunk chunk) {
        if (chunk.getLevel().isDebug()) {
            return DEBUG;
        }
        if (chunk.isEmpty()) {
            return EMPTY;
        }
        return new RenderChunk(chunk);
    }

    public @Nullable BlockEntity getBlockEntity(int x, int y, int z) {
        if (this.flags != 0) {
            return null;
        }
        assert this.blockEntities != null;
        return this.blockEntities.get(BlockPos.asLong(x, y, z));
    }

    public BlockState getBlockState(int x, int y, int z) {
        if (this.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }
        if ((this.flags & 2) != 0) {
            if (y == 60) {
                return Blocks.BARRIER.defaultBlockState();
            }
            if (y == 70) {
                return DebugLevelSource.getBlockStateFor(x, z);
            }
            return Blocks.AIR.defaultBlockState();
        }
        if (this.sections == null) {
            return Blocks.AIR.defaultBlockState();
        }
        assert this.wrapped != null;
        try {
            int index = this.wrapped.getSectionIndex(y);
            if (index >= 0 && index < this.sections.size()) {
                PalettedContainer<BlockState> palette = this.sections.get(index);
                if (palette != null) {
                    return palette.get(x & 15, y & 15, z & 15);
                }
            }
            return Blocks.AIR.defaultBlockState();
        }
        catch (Throwable t) {
            CrashReport crash = CrashReport.forThrowable(t, "Getting block state");
            CrashReportCategory category = crash.addCategory("Block being got");
            category.setDetail("Location", () -> CrashReportCategory.formatLocation(this.wrapped, x, y, z));
            throw new ReportedException(crash);
        }
    }

    public boolean isEmpty() {
        return (this.flags & 1) != 0;
    }

    public boolean isSectionEmpty(int posY) {
        if (this.isEmpty()) {
            return true;
        }
        if ((this.flags & 2) != 0) {
            return posY < 48 || posY >= 80;
        }
        if (this.sections == null) {
            return true;
        }
        assert this.wrapped != null;
        int index = this.wrapped.getSectionIndex(posY);
        if (index < 0 || index >= this.sections.size()) {
            return true;
        }
        PalettedContainer<BlockState> palette = this.sections.get(index);
        return palette == null;
    }
}
