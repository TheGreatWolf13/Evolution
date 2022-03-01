package tgw.evolution.mixin;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.blocks.IFluidLoggable;

import javax.annotation.Nullable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess {

    public LevelChunkMixin(ChunkPos p_187621_,
                           UpgradeData p_187622_,
                           LevelHeightAccessor p_187623_,
                           Registry<Biome> p_187624_,
                           long p_187625_,
                           @Nullable LevelChunkSection[] p_187626_,
                           @Nullable BlendingData p_187627_) {
        super(p_187621_, p_187622_, p_187623_, p_187624_, p_187625_, p_187626_, p_187627_);
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to implement new fluid system.
     */
    @Override
    @Overwrite
    public FluidState getFluidState(BlockPos pos) {
        int bx = pos.getX();
        int by = pos.getY();
        int bz = pos.getZ();
        try {
            int i = this.getSectionIndex(by);
            if (i >= 0 && i < this.sections.length) {
                LevelChunkSection chunksection = this.sections[i];
                if (!chunksection.hasOnlyAir()) {
                    BlockState state = chunksection.getBlockState(bx & 15, by & 15, bz & 15);
                    Block block = state.getBlock();
                    if (block instanceof IFluidLoggable fluidLoggable) {
                        return fluidLoggable.getFluidState(this.getLevel(), pos, state);
                    }
                    return state.getFluidState();
                }
            }
            return Fluids.EMPTY.defaultFluidState();
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting fluid state");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being got");
            crashreportcategory.setDetail("Location", () -> CrashReportCategory.formatLocation(this, bx, by, bz));
            throw new ReportedException(crashreport);
        }
    }

    @Shadow
    public abstract Level getLevel();
}
