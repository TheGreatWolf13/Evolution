package tgw.evolution.hooks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import tgw.evolution.blocks.IFluidLoggable;

public final class ChunkHooks {

    private ChunkHooks() {
    }

    /**
     * Hooks from {@link Chunk#getFluidState(BlockPos)}
     */
    @EvolutionHook
    public static IFluidState getFluidState(Chunk chunk, BlockPos pos) {
        int bx = pos.getX();
        int by = pos.getY();
        int bz = pos.getZ();
        try {
            if (by >= 0 && by >> 4 < chunk.getSections().length) {
                ChunkSection chunksection = chunk.getSections()[by >> 4];
                if (!ChunkSection.isEmpty(chunksection)) {
                    BlockState state = chunksection.getBlockState(bx & 15, by & 15, bz & 15);
                    Block block = state.getBlock();
                    if (block instanceof IFluidLoggable) {
                        return ((IFluidLoggable) block).getFluidState(chunk.getWorld(), pos, state);
                    }
                    return state.getFluidState();
                }
            }
            return Fluids.EMPTY.getDefaultState();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Getting fluid state");
            CrashReportCategory crashCategory = crashReport.makeCategory("Block being got");
            crashCategory.func_189529_a("Location", () -> CrashReportCategory.getCoordinateInfo(bx, by, bz));
            throw new ReportedException(crashReport);
        }
    }
}
