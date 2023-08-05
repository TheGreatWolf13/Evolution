package tgw.evolution.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;

@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity")
public abstract class MixinBoundTickingBlockEntity<T extends BlockEntity> implements TickingBlockEntity {

    @Shadow(aliases = "this$0") @Final LevelChunk field_27223;
    @Shadow @Final private T blockEntity;
    @Shadow private boolean loggedInvalidBlockState;
    @Shadow @Final private BlockEntityTicker<T> ticker;

    @Override
    @Overwrite
    public void tick() {
        if (!this.blockEntity.isRemoved() && this.blockEntity.hasLevel()) {
            BlockPos pos = this.blockEntity.getBlockPos();
            if (this.field_27223.isTicking(pos)) {
                try {
                    ProfilerFiller profiler = this.field_27223.level.getProfiler();
                    profiler.push(this::getType);
                    BlockState state = this.field_27223.getBlockState_(pos);
                    if (this.blockEntity.getType().isValid(state)) {
                        this.ticker.tick(this.field_27223.level, this.blockEntity.getBlockPos(), state, this.blockEntity);
                        this.loggedInvalidBlockState = false;
                    }
                    else if (!this.loggedInvalidBlockState) {
                        this.loggedInvalidBlockState = true;
                        Evolution.warn("Block entity {} @ {} state {} invalid for ticking:", LogUtils.defer(this::getType),
                                       LogUtils.defer(this::getPos), state);
                    }
                    profiler.pop();
                }
                catch (Throwable e) {
                    CrashReport crash = CrashReport.forThrowable(e, "Ticking block entity");
                    CrashReportCategory category = crash.addCategory("Block entity being ticked");
                    this.blockEntity.fillCrashReportCategory(category);
                    throw new ReportedException(crash);
                }
            }
        }
    }
}
