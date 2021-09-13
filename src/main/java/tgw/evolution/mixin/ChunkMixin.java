package tgw.evolution.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.blocks.IFluidLoggable;

@Mixin(Chunk.class)
public abstract class ChunkMixin extends CapabilityProvider<Chunk> {

    @Shadow
    @Final
    private ChunkSection[] sections;

    public ChunkMixin(Class<Chunk> baseClass) {
        super(baseClass);
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to implement new fluid system.
     */
    @Overwrite
    public FluidState getFluidState(BlockPos pos) {
        int bx = pos.getX();
        int by = pos.getY();
        int bz = pos.getZ();
        try {
            if (by >= 0 && by >> 4 < this.sections.length) {
                ChunkSection chunksection = this.sections[by >> 4];
                if (!ChunkSection.isEmpty(chunksection)) {
                    BlockState state = chunksection.getBlockState(bx & 15, by & 15, bz & 15);
                    Block block = state.getBlock();
                    if (block instanceof IFluidLoggable) {
                        return ((IFluidLoggable) block).getFluidState(this.getLevel(), pos, state);
                    }
                    return state.getFluidState();
                }
            }
            return Fluids.EMPTY.defaultFluidState();
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting fluid state");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being got");
            crashreportcategory.setDetail("Location", () -> CrashReportCategory.formatLocation(bx, by, bz));
            throw new ReportedException(crashreport);
        }
    }

    @Shadow
    public abstract World getLevel();
}
