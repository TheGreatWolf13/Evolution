package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.patches.ILevelPatch;

import java.util.Random;
import java.util.function.Supplier;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

    private final BlockPos.MutableBlockPos randomPosInChunkCachedPos = new BlockPos.MutableBlockPos();

    public ServerLevelMixin(WritableLevelData pLevelData,
                            ResourceKey<Level> pDimension,
                            DimensionType pDimensionType,
                            Supplier<ProfilerFiller> pProfiler,
                            boolean pIsClientSide,
                            boolean pIsDebug,
                            long pBiomeZoomSeed) {
        super(pLevelData, pDimension, pDimensionType, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed);
    }

    @Override
    @Shadow
    public abstract void blockUpdated(BlockPos pPos, Block pBlock);

    /**
     * Ensure an immutable block position is passed on block tick
     */
    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick" +
                                                                        "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;" +
                                                                        "Ljava/util/Random;)V"))
    private void proxyBlockStateTick(BlockState blockState, ServerLevel level, BlockPos pos, Random rand) {
        blockState.randomTick(level, pos.immutable(), rand);
    }

    /**
     * Ensure an immutable block position is passed on fluid tick
     */
    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;randomTick" +
                                                                        "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;" +
                                                                        "Ljava/util/Random;)V"))
    private void proxyFluidStateTick(FluidState fluidState, Level level, BlockPos pos, Random rand) {
        fluidState.randomTick(level, pos.immutable(), rand);
    }

    /**
     * Avoid allocating BlockPos every invocation through using our allocation-free variant
     */
    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockRandomPos(IIII)" +
                                                                        "Lnet/minecraft/core/BlockPos;"))
    private BlockPos proxyTickGetRandomPosInChunk(ServerLevel level, int x, int y, int z, int mask) {
        ((ILevelPatch) level).getRandomPosInChunk(x, y, z, mask, this.randomPosInChunkCachedPos);
        return this.randomPosInChunkCachedPos;
    }
}
